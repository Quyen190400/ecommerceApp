#!/bin/bash

# Configuration
IMAGE_NAME="nthquyen1904/greentea-app"
TAG="latest"
FULL_IMAGE_NAME="$IMAGE_NAME:$TAG"

echo "🚀 Building JAR with Docker and creating image: $FULL_IMAGE_NAME"

# Step 1: Build JAR using Docker container
echo "📦 Building JAR file using Docker..."
docker run --rm -v "$(pwd):/app" -w /app eclipse-temurin:17-jdk-alpine \
    sh -c "chmod +x ./gradlew && ./gradlew build -x test --no-daemon"

if [ $? -eq 0 ]; then
    echo "✅ JAR build successful!"
    
    # Step 2: Build Docker image using JAR
    echo "🐳 Building Docker image..."
    docker build -f Dockerfile.jar -t $FULL_IMAGE_NAME .
    
    if [ $? -eq 0 ]; then
        echo "✅ Docker build successful!"
        
        # Test the image locally (optional)
        echo "🧪 Testing image locally..."
        docker run --rm -d --name test-greentea-app -p 8080:8080 $FULL_IMAGE_NAME
        sleep 15
        
        # Check if container is running
        if docker ps | grep -q test-greentea-app; then
            echo "✅ Local test successful! Container is running."
            echo "🌐 You can test the app at: http://localhost:8080"
            echo "⏹️  Stopping test container..."
            docker stop test-greentea-app
        else
            echo "❌ Local test failed! Container is not running."
            docker logs test-greentea-app
            docker stop test-greentea-app 2>/dev/null
            echo "⚠️  Continuing anyway for push..."
        fi
        
        # Push to Docker Hub
        echo "📤 Pushing to Docker Hub..."
        docker push $FULL_IMAGE_NAME
        
        if [ $? -eq 0 ]; then
            echo "✅ Push successful!"
            echo "🎉 Image is now available at: $FULL_IMAGE_NAME"
            echo ""
            echo "📋 Next steps:"
            echo "1. Deploy to Railway using the image: $FULL_IMAGE_NAME"
            echo "2. Set environment variables in Railway dashboard"
            echo "3. Configure port 8080 in Railway"
        else
            echo "❌ Push failed!"
            exit 1
        fi
    else
        echo "❌ Docker build failed!"
        exit 1
    fi
else
    echo "❌ JAR build failed!"
    exit 1
fi 