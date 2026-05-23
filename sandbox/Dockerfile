# Use Eclipse Temurin 23 JDK (the official replacement for openjdk:* images)
FROM eclipse-temurin:23-jdk

# Install Python 3, pip, GNU time, and C/C++ build tools
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    gcc \
    g++ \
    make \
    time && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

