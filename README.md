# Automatic License Plate Recognition System (ALPR)

## Overview

This project is an Automatic License Plate Recognition (ALPR) system using Computer Vision and OCR.

The system consists of two parts:

- Backend: Python Flask server with OpenCV and EasyOCR
- Android App: Mobile client for capturing/selecting vehicle images and sending them to the server

The Android application does not perform recognition locally. Instead, it sends the image to the backend server where all image processing and OCR are executed.



## Features

- Capture image using Android camera
- Select image from gallery
- Send image to Flask server
- Detect license plate region
- Recognize plate text using OCR
- Display processed image with bounding box
- Show processing time
- Store recognition history inside the app



## Technical Submission

### Source Code

The project is divided into two main components:

- backend/ – Python Flask server implementing the ALPR pipeline
- android_app/ – Android application for user interaction

The code is structured, readable, and reproducible.



### Requirements File

All required Python libraries are listed in:

backend/requirements.txt

Main dependencies:
- flask
- opencv-python
- numpy
- easyocr

To install:

pip install -r requirements.txt



### Trained Model Weights

This project does not use a custom-trained deep learning model.

Instead, it relies on:
- EasyOCR (pre-trained OCR model)
- OpenCV-based image processing

Therefore, no .h5 or .pth files are required.



### Dataset / Data Source

The dataset consists of:
- manually captured images using the mobile application
- publicly available vehicle images from the internet

No fixed dataset is provided.

To reproduce results:
1. Use any vehicle image with a visible license plate
2. Run the backend server
3. Send the image from the Android app



### Reproducibility

To run the project:

1. Start backend:
cd backend
pip install -r requirements.txt
python app.py

2. Run Android app in Android Studio

3. Make sure:
- Phone and PC are connected to the same Wi-Fi network
- Correct server IP is set in MainActivity.kt

4. Capture or select an image and press "Recognize Plate"



## Project Structure

```text
ALPR-System/
│
├── backend/
│   ├── app.py
│   ├── requirements.txt
│   ├── core/
│   │   ├── preprocessing.py
│   │   ├── detection.py
│   │   ├── ocr.py
│   │   └── pipeline.py
│   ├── utils/
│   │   └── logger.py
│   └── uploads/
│
├── android_app/
│   ├── app/
│   ├── gradle/
│   ├── build.gradle.kts
│   └── settings.gradle.kts
│
└── README.md
```



## APK Build

android_app/app/build/outputs/apk/debug/app-debug.apk




## Future Improvements

- YOLO-based license plate detection
- Real-time video recognition
- Improved OCR models
- Cloud deployment
- Offline recognition on Android



## Technologies Used

Backend:
- Python
- Flask
- OpenCV
- EasyOCR
- NumPy

Android:
- Kotlin
- Android Studio
- OkHttp
- Camera Intent API


