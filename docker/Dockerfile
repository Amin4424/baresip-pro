# ==============================================================================
# Baresip+ Android Build Container
# Ubuntu 24.04 + OpenJDK 21 + Android SDK 37 + NDK 29.0.14206865 + CMake
# ==============================================================================

FROM ubuntu:24.04

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install core build dependencies and system tools
RUN apt-get update && apt-get install -y --no-install-recommends \
    bash \
    curl \
    wget \
    unzip \
    git \
    make \
    ninja-build \
    pkg-config \
    libtool \
    automake \
    autoconf \
    m4 \
    python3 \
    ca-certificates \
    openjdk-21-jdk \
    && rm -rf /var/lib/apt/lists/*

# Environment variables for Android SDK & Java
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_NDK_VERSION=29.0.14206865
ENV ANDROID_NDK_HOME=${ANDROID_SDK_ROOT}/ndk/${ANDROID_NDK_VERSION}
ENV NDK_PATH=${ANDROID_NDK_HOME}
ENV PATH=${PATH}:${JAVA_HOME}/bin:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/tools/bin:${ANDROID_NDK_HOME}

# Download and install Android Command Line Tools
ARG CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools && \
    wget -q ${CMDLINE_TOOLS_URL} -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools && \
    mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# Accept all Android licenses and install required SDK & NDK packages
RUN yes | sdkmanager --licenses && \
    sdkmanager \
        "platform-tools" \
        "platforms;android-37.0" \
        "platforms;android-36" \
        "build-tools;36.0.0" \
        "ndk;${ANDROID_NDK_VERSION}" \
        "cmake;3.31.6" && \
    if [ -d "${ANDROID_SDK_ROOT}/platforms/android-37.0" ] && [ ! -d "${ANDROID_SDK_ROOT}/platforms/android-37" ]; then \
        ln -s "${ANDROID_SDK_ROOT}/platforms/android-37.0" "${ANDROID_SDK_ROOT}/platforms/android-37"; \
    fi

# Create build workspace directory
WORKDIR /workspace

# Copy build script into container
COPY build.sh /usr/local/bin/build-app
RUN chmod +x /usr/local/bin/build-app

# Default command
CMD ["/usr/local/bin/build-app", "release"]
