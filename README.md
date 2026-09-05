<p align="center">
  <img src="docs/images/logo_baresip_pro.png" width="460" alt="Baresip Pro" />
</p>

<h1 align="center">Baresip Pro</h1>

<p align="center">
  <b>Modern, Private & High-Definition SIP Voice Softphone for Android</b>
</p>

<p align="center">
  <a href="https://github.com/Amin4424/baresip-pro-max/releases/latest">
    <img src="https://img.shields.io/github/v/release/Amin4424/baresip-pro-max?color=blue&label=Download%20APK&logo=android" alt="Download APK" />
  </a>
  <img src="https://img.shields.io/badge/Application%20ID-io.github.amin4424.baresip.pro-blue" alt="Application ID" />
  <img src="https://img.shields.io/badge/Android-9.0%2B%20(API%2028%2B)-brightgreen?logo=android" alt="Android Version" />
  <img src="https://img.shields.io/badge/Architecture-arm64--v8a%20%7C%20armeabi--v7a%20%7C%20x86__64-orange" alt="Supported Architectures" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose%20%7C%20Material%203-purple?logo=jetpackcompose" alt="Material 3" />
  <img src="https://img.shields.io/badge/License-BSD--3--Clause-blue.svg" alt="License" />
</p>

---

## 📱 About Baresip Pro

**Baresip Pro** is a modern, privacy-first SIP (Session Initiation Protocol) client designed specifically for lightweight, high-performance voice communications on Android. It pairs the battle-tested, standard-compliant Baresip C audio engine with a completely reimagined, fluid **Jetpack Compose (Material 3)** interface.

Whether connecting to enterprise PBXs (Asterisk, FreePBX, Kamailio, OpenSIPS) or public SIP providers, Baresip Pro gives you full control over your communications without proprietary lock-in, tracking, or third-party telemetry.

* **Looking for Video Calling?** Check out our sister edition **Baresip Pro Max** (on the `video-ui-modern` branch), which includes full AV1/H.265/H.264 video streaming and Camera2 support.

---

## 💡 Why Baresip Pro? (Fork Philosophy)

**Baresip Pro** is an independent fork of [baresip-studio](https://github.com/juha-h/baresip-studio) by Juha Heinanen.

* **🎯 Completely Modernized UI**: Fluid **Jetpack Compose** interface following Material 3 design guidelines. Offers one-handed bottom navigation, an ergonomic keypad dialer, modular call screens, smooth animations, dynamic colors, and automatic dark/light theme switching.
* **🎙️ Pure Voice-Only Focus**: Stripped of heavy video dependencies (no FFmpeg, x264, or VPX bloat), Baresip Pro delivers an ultra-compact APK size, lightning-fast startup, and minimal memory footprint.
* **⚡ Trusted Core Engine**: Powered by `libbaresip`, `libre`, and OpenSSL. All media handling, ZRTP/SRTP encryption, and network transports execute directly in optimized native C code.
* **🔒 Strict Privacy**: Zero trackers, no third-party telemetry, and no dependency on Google Cloud Messaging (GCM/FCM).

---

## ✨ Key Features

### 🎙️ Crystal-Clear HD Audio
- **Wideband & Fullband Codecs**: Support for **Opus**, **AMR-WB**, **AMR-NB**, **G.722**, **G.722.1**, **G.729**, **iLBC**, **Codec2**, and standard **G.711 (PCMA/PCMU)**.
- **Low-Latency Audio**: Optimized for Android AAudio and OpenSL ES sound systems to minimize delay and packet jitter.

### 🔒 Privacy & Military-Grade Security
- **End-to-End Media Encryption**: Full support for **ZRTP** and **SRTP** (SDES and DTLS-SRTP).
- **Secure Signaling**: Encrypted SIP transport using **TLS** and **WSS**.
- **Zero Tracking**: 100% open-source, no analytical trackers, and no third-party data collection.

### 🎨 Modern Material 3 Design
- **Intuitive Navigation**: Floating pill-shaped bottom navigation bar for quick access to Keypad, Calls, Contacts, and Messages.
- **Modular Call Screen**: Dedicated interface for incoming, outgoing, active, and hold states with audio gain controls and call recording.
- **Multi-Account Support**: Manage multiple SIP accounts simultaneously with live registration status indicators.
- **Call Recording**: Built-in high-fidelity local call recording.
- **Messaging & History**: Support for SIP instant messaging, contact synchronization, and call logs.

---

## 📸 Screenshots

<p align="center">
  <img src="docs/images/screen_main.png" width="220" alt="Main Dialer" />
  &nbsp;
  <img src="docs/images/screen_call.png" width="220" alt="Call Screen" />
  &nbsp;
  <img src="docs/images/screen_history.png" width="220" alt="Call History" />
  &nbsp;
  <img src="docs/images/screen_chats.png" width="220" alt="Chat & Messaging" />
</p>

---

## 📥 Download & Installation

You can download pre-built signed APKs directly from GitHub [**Releases**](https://github.com/Amin4424/baresip-pro-max/releases):

| APK Variant | Target Devices | Architecture | Notes |
| :--- | :--- | :--- | :--- |
| **`*-arm64-v8a.apk`** | Modern Android Phones & Tablets | `arm64-v8a` | 🌟 **Recommended** for 99% of modern Android devices |
| **`*-armeabi-v7a.apk`** | Older 32-bit Android Devices | `armeabi-v7a` | Compatible with legacy Android hardware |
| **`*-universal.apk`** | Any Supported Android Device | Multi-ABI | **Global package** — contains all native CPU architectures |
| **`*-x86_64.apk`** | Android Studio Emulator / PC / ChromeOS | `x86_64` | Tailored for 64-bit Intel & AMD systems |

> **System Requirements**: Android 9.0 (Pie / API level 28) or higher.

---

## 📄 License & Credits

- **License**: Distributed under the [BSD 3-Clause License](LICENSE).
- **Core Engine**: Developed by Alfred E. Heggestad and the [Creytiv Baresip community](https://github.com/baresip/baresip).
- **Upstream Baresip Studio**: Originally authored by Juha Heinanen / [TutPro Inc.](https://github.com/juha-h/baresip-studio).
- **Baresip Pro**: Modern Jetpack Compose UI, architecture improvements, and maintenance by [Amin4424](https://github.com/Amin4424).
