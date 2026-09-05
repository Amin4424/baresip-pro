#!/usr/bin/env bash
# ==============================================================================
# Baresip+ Build Script (used inside Docker and in CI/local environments)
# ==============================================================================

set -euo pipefail

BUILD_TYPE="${1:-release}"
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUTPUT_DIR="${PROJECT_ROOT}/output"

echo "=================================================="
echo " Starting Baresip+ Build"
echo " Build Type : ${BUILD_TYPE}"
echo " Project Root: ${PROJECT_ROOT}"
echo " Output Dir : ${OUTPUT_DIR}"
echo "=================================================="

cd "${PROJECT_ROOT}"

# Ensure gradlew is executable
chmod +x ./gradlew

# Ensure output directory exists
mkdir -p "${OUTPUT_DIR}"

# Check for native precompiled distribution directory
if [ ! -d "${PROJECT_ROOT}/distribution.video" ]; then
    echo "⚠️  WARNING: distribution.video directory not found at ${PROJECT_ROOT}/distribution.video"
    if [ -f "${PROJECT_ROOT}/distribution.video.tar.xz" ]; then
        echo "📦 Extracting distribution.video.tar.xz..."
        tar -xf "${PROJECT_ROOT}/distribution.video.tar.xz" -C "${PROJECT_ROOT}"
    elif [ -f "${PROJECT_ROOT}/distribution.video.tar.gz" ]; then
        echo "📦 Extracting distribution.video.tar.gz..."
        tar -xzf "${PROJECT_ROOT}/distribution.video.tar.gz" -C "${PROJECT_ROOT}"
    else
        echo "❌ distribution.video is missing. The native CMake build requires distribution.video/"
        echo "   Please make sure distribution.video is available before building."
        exit 1
    fi
fi

# Execute build based on requested target
case "${BUILD_TYPE}" in
    release)
        echo "🚀 Running ./gradlew assembleRelease..."
        ./gradlew assembleRelease --no-daemon --stacktrace
        ;;
    debug)
        echo "🚀 Running ./gradlew assembleDebug..."
        ./gradlew assembleDebug --no-daemon --stacktrace
        ;;
    both)
        echo "🚀 Running ./gradlew assembleDebug assembleRelease..."
        ./gradlew assembleDebug assembleRelease --no-daemon --stacktrace
        ;;
    *)
        echo "❌ Unknown build type: ${BUILD_TYPE}. Supported: release, debug, both"
        exit 1
        ;;
esac

# Collect generated APKs
echo "📦 Collecting build outputs..."
find "${PROJECT_ROOT}/app/build/outputs/apk" -type f -name "*.apk" | while read -r apk_file; do
    echo "  Found APK: ${apk_file}"
    cp -v "${apk_file}" "${OUTPUT_DIR}/"
done

# Generate SHA256 checksums
cd "${OUTPUT_DIR}"
if compgen -G "*.apk" > /dev/null; then
    sha256sum *.apk > SHA256SUMS.txt
    echo "✅ Generated SHA256SUMS.txt:"
    cat SHA256SUMS.txt
else
    echo "⚠️  No APK files found in ${OUTPUT_DIR}"
fi

echo "=================================================="
echo "🎉 Build finished successfully!"
echo "Outputs stored in: ${OUTPUT_DIR}"
echo "=================================================="
