# Automatic License Plate Recognition System(ALPR)

## Overview

This project is an Automatic License Plate Recognition (ALPR) system using Computer Vision and OCR.

The system consists of two parts:

- **Backend:** Python Flask server with OpenCV and EasyOCR
- **Android App:** Mobile client for capturing/selecting vehicle images and sending them to the server

The Android app does not recognize license plates locally. It sends the image to the backend server, where all image processing and OCR are performed.



## Features

- Capture image using Android camera
- Select image from gallery
- Send image to Flask server
- Detect license plate region
- Recognize plate text using OCR
- Display recognized plate number
- Display processed image with bounding box
- Show processing time
- Store recognition history inside the app



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