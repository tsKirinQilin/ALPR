package com.example.alprapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var cameraButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var recognizeButton: Button
    private lateinit var carImageView: ImageView
    private lateinit var resultTextView: TextView
    private lateinit var loadingBar: ProgressBar
    private lateinit var historyTextView: TextView

    private var selectedImageUri: Uri? = null
    private var currentPhotoFile: File? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    private val serverUrl = "http://192.168.1.17:5000/recognize"

    private val historyList = mutableListOf<String>()

    private val galleryPicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                currentPhotoFile = null
                carImageView.setImageURI(selectedImageUri)
                resultTextView.text = "Image selected"
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = null
                carImageView.setImageURI(Uri.fromFile(currentPhotoFile))
                resultTextView.text = "Photo captured"
            } else {
                resultTextView.text = "Camera cancelled"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraButton = findViewById(R.id.cameraButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        recognizeButton = findViewById(R.id.recognizeButton)
        carImageView = findViewById(R.id.carImageView)
        resultTextView = findViewById(R.id.resultTextView)
        loadingBar = findViewById(R.id.loadingBar)
        historyTextView = findViewById(R.id.historyTextView)

        cameraButton.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        selectImageButton.setOnClickListener { selectImage() }
        recognizeButton.setOnClickListener { uploadImage() }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            takePhoto()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                100
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            takePhoto()
        } else {
            resultTextView.text = "Camera permission denied"
        }
    }

    private fun takePhoto() {
        try {
            val photoFile = File.createTempFile(
                "camera_photo_",
                ".jpg",
                cacheDir
            )

            currentPhotoFile = photoFile

            val photoUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                photoFile
            )

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            cameraLauncher.launch(intent)

        } catch (e: Exception) {
            resultTextView.text = "Camera error: ${e.message}"
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryPicker.launch(intent)
    }

    private fun uploadImage() {

        val imageFile: File = when {
            currentPhotoFile != null -> currentPhotoFile!!
            selectedImageUri != null -> uriToFile(selectedImageUri!!)
            else -> {
                resultTextView.text = "Select image first"
                return
            }
        }

        loadingBar.visibility = View.VISIBLE
        resultTextView.text = "Processing..."

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                imageFile.name,
                imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    loadingBar.visibility = View.GONE
                    resultTextView.text = "Error: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string()

                runOnUiThread {
                    loadingBar.visibility = View.GONE

                    try {
                        val json = JSONObject(responseText ?: "")
                        val success = json.getBoolean("success")

                        if (success) {
                            val plateText = json.getString("plate_text")
                            val time = json.getDouble("processing_time")

                            val imageBase64 = json.getString("result_image")
                            val bytes = Base64.decode(imageBase64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            carImageView.setImageBitmap(bitmap)

                            resultTextView.text = "Plate: $plateText\nTime: ${time}s"

                            historyList.add("$plateText (${time}s)")
                            historyTextView.text = historyList.joinToString("\n")

                        } else {
                            resultTextView.text = "Plate not detected"
                        }

                    } catch (e: Exception) {
                        resultTextView.text = "Invalid response"
                    }
                }
            }
        })
    }

    private fun uriToFile(uri: Uri): File {
        val fileName = getFileName(uri)
        val file = File(cacheDir, fileName)

        contentResolver.openInputStream(uri).use { input ->
            FileOutputStream(file).use { output ->
                input?.copyTo(output)
            }
        }

        return file
    }

    private fun getFileName(uri: Uri): String {
        var name = "image.jpg"
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index >= 0) {
                name = it.getString(index)
            }
        }
        return name
    }
}