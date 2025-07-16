# Multi-stage build for Spring Boot application
FROM eclipse-temurin:17-jdk-alpine AS build

# Install necessary packages
RUN apk add --no-cache curl unzip

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x ./gradlew

# Set Gradle properties to avoid network issues
RUN echo "org.gradle.daemon=false" > gradle.properties && \
    echo "org.gradle.parallel=true" >> gradle.properties && \
    echo "org.gradle.configureondemand=true" >> gradle.properties

# Download dependencies with retry mechanism
RUN ./gradlew dependencies --no-daemon --stacktrace || \
    (sleep 10 && ./gradlew dependencies --no-daemon --stacktrace) || \
    (sleep 20 && ./gradlew dependencies --no-daemon --stacktrace)

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Create app user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads/images && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
