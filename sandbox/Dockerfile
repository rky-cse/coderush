# Use the official OpenJDK 23 slim image (Debian-based)
FROM openjdk:23-slim

# Install Python 3, pip, GNU time, and C/C++ build tools
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    gcc \
    g++ \
    make \
    time && \
    apt-get clean && rm -rf /var/lib/apt/lists/*


