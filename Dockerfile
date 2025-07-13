# Stage 1: chỉ copy jar đã build
FROM openjdk:17-slim as runtime

WORKDIR /app

# Chỉ copy file jar (build sẵn từ local)
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
