# Docker Build Environment for Baresip+

This folder contains containerized build configurations for reproducible builds of Baresip+ Android application across any system (Linux, macOS, Windows with Docker / WSL2).

## What's Included

- **`Dockerfile`**: Ubuntu 24.04 image with OpenJDK 21, Android SDK 37, NDK `29.0.14206865`, and CMake.
- **`docker-compose.yml`**: Preconfigured service definitions with persistent Gradle caching.
- **`build.sh`**: Autonomous build script handling task execution, APK collection, and SHA256 checksum generation.

---

## Quick Start with Docker Compose

### 1. Build Release APK

```bash
docker compose -f docker/docker-compose.yml run --rm build-release
```

### 2. Build Debug APK

```bash
docker compose -f docker/docker-compose.yml run --rm build-debug
```

Outputs will be saved in the `output/` directory:
- `output/app-release.apk`
- `output/SHA256SUMS.txt`

---

## Building Directly with Docker CLI

### Step 1: Build Image

```bash
docker build -t baresip-builder -f docker/Dockerfile .
```

### Step 2: Run Build

```bash
docker run --rm -v "$(pwd):/workspace" baresip-builder /workspace/docker/build.sh release
```

---

## Requirements & Prerequisites

1. **`distribution.video/`**: The native C/C++ engine requires precompiled static libraries in `distribution.video/`. Ensure this folder exists in your project root before triggering builds.
2. **Keystore**: The release signing keystore is located at `app/baresip-promax-release.keystore` and configured inside `app/build.gradle.kts`.
