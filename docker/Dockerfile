# Use RockyLinux 9 directly as we will use pre-built JAR
FROM rockylinux:9

# Set working directory
WORKDIR /app

# 1. Configure Aliyun Yum Source for Rocky Linux 9
RUN sed -e 's|^mirrorlist=|#mirrorlist=|g' \
        -e 's|^#baseurl=http://dl.rockylinux.org/$contentdir|baseurl=https://mirrors.aliyun.com/rockylinux|g' \
        -i.bak \
        /etc/yum.repos.d/rocky-*.repo && \
    dnf makecache

# 2. Install Java 21, Python 3.12, and Pip
# Rocky Linux 9 AppStream usually has java-21-openjdk and python3.12
RUN dnf install -y java-21-openjdk-headless python3.12 python3.12-pip mesa-libGL && \
    dnf clean all

# 3. Configure Aliyun PyPI Mirror and Install Python Dependencies
# Increase timeout and split installation to handle large files (like torch) better
# Install CPU-only version of PyTorch to avoid downloading massive CUDA dependencies
RUN pip3.12 config set global.index-url https://mirrors.aliyun.com/pypi/simple/ && \
    pip3.12 install --no-cache-dir --default-timeout=1000 torch torchvision --index-url https://download.pytorch.org/whl/cpu

# Copy requirements.txt and install other dependencies
# We copy this AFTER installing torch to better utilize layer caching
COPY back/src/main/resources/scripts/requirements.txt .
RUN pip3.12 install --no-cache-dir --default-timeout=1000 -r requirements.txt

# 4. Copy the pre-built JAR file from host
# Make sure to run 'mvn clean package' on host first!
COPY back/target/*.jar app.jar

# 5. Copy models
# Assuming models are in back/models in the source context
COPY back/models ./models

# 6. Copy python scripts
# Since we are not building from source, we need to ensure scripts are available
RUN mkdir -p target/classes/scripts
COPY back/src/main/resources/scripts/detect.py target/classes/scripts/

# Create uploads directory
RUN mkdir uploads

# Expose port
EXPOSE 8080

# Environment variables
ENV JAVA_OPTS=""

# Command to run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
