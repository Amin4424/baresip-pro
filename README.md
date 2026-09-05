# Baresip+ (Studio) — Modern SIP & Video Client for Android

[![Build & Release](https://github.com/Amin4424/baresip-studio/actions/workflows/release.yml/badge.svg)](https://github.com/Amin4424/baresip-studio/actions/workflows/release.yml)
[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-28-blue.svg)](https://developer.android.com/about/versions/pie)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-green.svg)](https://developer.android.com/about/versions/16)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4-purple.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL%203.0-orange.svg)](LICENSE)

**Baresip+** is a secure, high-performance SIP user agent for Android featuring hardware-accelerated HD voice and video calling, end-to-end media encryption, and a modernized interface built with **Jetpack Compose** and **Material 3**.

---

## ✨ Features

- **🎥 High-Definition Video Calling**:
  - Support for next-generation video codecs: **AV1**, **H.264**, **H.265 (HEVC)**, **VP8**, and **VP9**.
  - Camera2 API integration with camera switching (front/back), live preview, and aspect-ratio matching.
- **🎙️ Crystal-Clear Audio**:
  - Codec support: **Opus**, **AMR-WB**, **AMR-NB**, **G.722**, **G.722.1**, **G.729**, **PCMA/PCMU (G.711)**, **iLBC**, and **Codec2**.
  - Low-latency audio using Android AAudio / OpenSL ES.
- **🔒 Robust Security**:
  - Media encryption via **ZRTP** and **SRTP** (SDES / DTLS-SRTP).
  - Signaling encryption over **TLS**.
- **📱 Modern Jetpack Compose UI**:
  - Modernized bottom navigation bar with fluid transitions.
  - Dedicated call screens for incoming, outgoing, active, and hold states.
  - Built-in call recording with playback support.
  - Multi-account management with live SIP registration statuses.
  - History, contact directory, and instant text messaging.

---

## 🏗️ Project Architecture

The application combines a high-speed native C/C++ communications core with a modern Kotlin Compose frontend:

```
baresip-studio/
├── .github/workflows/         # Automated GitHub Actions CI/CD pipelines
│   └── release.yml            # Tag-triggered APK compilation & GitHub Releases
├── app/                       # Android application module
│   ├── src/main/kotlin/       # Jetpack Compose UI, ViewModels, and SIP services
│   ├── src/main/cpp/          # JNI bridge, CMake build file, and camera pipelines
│   └── baresip-promax-release.keystore # Signing configuration
├── distribution.video/        # Precompiled native static & shared C libraries (ARM64 & x86_64)
├── docker/                    # Isolated containerized build environment
│   ├── Dockerfile             # Ubuntu 24.04 + JDK 21 + Android SDK 37 + NDK 29
│   ├── docker-compose.yml     # Compose services with Gradle cache volumes
│   ├── build.sh               # Unified build script with SHA256 generation
│   └── README.md              # Container build documentation
└── libbaresip-android.video/  # Submodule containing source code for native dependencies
```

---

## 🚀 Building the Project

### Option A: Local Build with Gradle Wrapper

#### Prerequisites
- **JDK 21** (e.g. OpenJDK 21 or Eclipse Temurin)
- **Android SDK** (Compile SDK: 37, Platform Tools, Build Tools 35.0.0+)
- **Android NDK**: `29.0.14206865`
- **CMake**: `3.22.1` or higher

```bash
# Clone with submodules
git clone --recurse-submodules https://github.com/Amin4424/baresip-studio.git
cd baresip-studio

# Assemble Debug APK
./gradlew assembleDebug

# Assemble Release APK (signed)
./gradlew assembleRelease
```

Generated APKs will be located under `app/build/outputs/apk/`.

---

### Option B: Building with Docker (Zero Setup)

You can build the app inside an isolated container with all SDKs and NDKs pre-configured:

```bash
# Build Release APK
docker compose -f docker/docker-compose.yml run --rm build-release

# Build Debug APK
docker compose -f docker/docker-compose.yml run --rm build-debug
```

Outputs and `SHA256SUMS.txt` checksums will be saved in the `output/` directory.

---

## 📦 Automated GitHub CI/CD & Releases

This repository includes a continuous integration and release pipeline configured in [`.github/workflows/release.yml`](.github/workflows/release.yml).

### Creating a New Release

Whenever you push a Git tag matching `v*` (e.g., `v1.0.0`), GitHub Actions will:
1. Check out the code and submodules recursively.
2. Configure OpenJDK 21 and the required Android SDK / NDK toolchains.
3. Build the signed release APK.
4. Compute SHA-256 checksums (`SHA256SUMS.txt`).
5. Upload artifacts to the workflow run.
6. Publish an official **GitHub Release** containing the downloadable APK and checksums.

#### Step-by-Step Command:

```bash
# 1. Commit your latest changes
git add .
git commit -m "Release v1.0.0"

# 2. Tag your commit
git tag -a v1.0.0 -m "Release v1.0.0 - Baresip+ Modern UI"

# 3. Push commit and tag to GitHub
git push origin <your-branch>
git push origin v1.0.0
```

### Manual Trigger

You can also trigger builds manually without a tag:
1. Navigate to the **Actions** tab in your GitHub repository.
2. Select **Build & Release Baresip+**.
3. Click **Run workflow** and choose your build type (`release`, `debug`, or `both`).

---

## 📄 License & Acknowledgements

- **License**: GNU General Public License v3.0 ([GPL-3.0](LICENSE)).
- **Upstream Project**: Based on the [baresip](https://github.com/baresip/baresip) project by Alfred E. Heggestad and [baresip-studio](https://github.com/juha-h/baresip-studio) by Juha Heinanen.
