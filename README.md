# Classcau

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com/)
[![OpenCV](https://img.shields.io/badge/OpenCV-3.4.3-blue.svg)](https://opencv.org/)

---

## 📸 Real-Time Image Segmentation on Android with OpenCV

**Classcau** is an open-source Android application that leverages the power of [OpenCV](https://opencv.org/) for real-time image segmentation and camera-based processing. Designed for developers, researchers, and enthusiasts, this project demonstrates how to integrate advanced computer vision techniques into a modern Android app, with a focus on usability, modularity, and performance.

---

## 🚀 Features

- **Real-Time Camera Preview:** Utilizes Android's Camera2 API for high-performance camera streaming.
- **Image Segmentation:** Segments images based on regions of interest (ROI) using OpenCV, with transparency and edge detection.
- **Custom Overlays:** Draws interactive overlays to guide users during image capture.
- **Modular Architecture:** Clean separation of model and view components for easy extension and maintenance.
- **Tablet-Optimized UI:** Responsive design for various screen sizes.
- **OpenCV Integration:** Includes a local OpenCV 3.4.3 library for advanced image processing.

---

## 🖼️ Screenshots

> **Add your screenshots here for maximum impact!**
>
> ![Camera Preview](docs/screenshots/camera_preview.png)
> ![Image Segmentation](docs/screenshots/image_segmentation.png)

---

## 🛠️ Tech Stack

- **Language:** Java
- **Platform:** Android (minSdkVersion 21, targetSdkVersion 28)
- **Libraries:**
  - [OpenCV 3.4.3](https://opencv.org/)
  - Android Support Libraries (AppCompat, ConstraintLayout)

---

## 📂 Project Structure

```
app/
  └── src/main/java/br/com/patricksferraz/classcau/
      ├── model/         # Image segmentation logic (OpenCV)
      └── view/          # Activities and custom views (Camera, Canvas, Auth)
openCVLibrary343/        # Local OpenCV Android library module
```

---

## ⚡ Getting Started

### Prerequisites
- [Android Studio](https://developer.android.com/studio)
- Android SDK 28+
- Java 8+

### Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/patricksferraz/classcau-android.git
   cd classcau-android
   ```
2. **Open in Android Studio:**
   - File > Open > Select the project directory.
3. **Build the project:**
   - Gradle will automatically download dependencies.
4. **Run on a device or emulator:**
   - Grant camera and storage permissions when prompted.

---

## 📖 Usage

- Launch the app. The main screen opens the camera preview.
- Position the object (e.g., a cutting board) within the highlighted rectangle.
- Tap the capture button to segment the image based on the region of interest.
- Segmented images are processed and can be saved or further analyzed.

---

## 🤝 Contributing

Contributions are welcome! To get started:
1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/your-feature`)
5. Open a Pull Request

Please see [CONTRIBUTING.md](CONTRIBUTING.md) for more details.

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 🙏 Credits

- Developed by [Patrick Silva Ferraz](https://github.com/patricksferraz)
- Powered by [OpenCV](https://opencv.org/)
- Inspired by the open-source and Android developer communities

---

## 🌐 SEO & Discoverability

> Android, OpenCV, Image Segmentation, Real-Time Processing, Computer Vision, Camera2 API, Java, Open Source, Mobile Development, Pattern Recognition, ROI, Cutting Board Detection

---

> _"Empowering Android with real-time computer vision. Fork, star, and contribute!"_
