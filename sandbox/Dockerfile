# Use the official OpenJDK 23 slim image (Debian-based)
FROM openjdk:23-slim

# Install Python 3, pip, and C/C++ build tools
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    gcc \
    g++ \
    make && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Set default command to open a bash shell
COPY . .
CMD ["java","-jar","judge.jar"]
