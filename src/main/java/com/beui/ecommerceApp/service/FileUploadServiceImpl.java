package com.beui.ecommerceApp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileUploadServiceImpl implements FileUploadService {
    @Value("${app.upload.path:uploads/images}")
    private String uploadPath;

    @Override
    public ResponseEntity<String> uploadImage(MultipartFile file, String customName) {
        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // Luôn random tên file (UUID)
            String fileName = java.util.UUID.randomUUID().toString() + fileExtension;
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();
            File dest = new File(uploadDir, fileName);
            file.transferTo(dest);
            return ResponseEntity.ok(fileName); // chỉ trả về tên file
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> uploadImageWithCustomName(MultipartFile file, String customName) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String cleanCustomName = customName.replaceAll("[^a-zA-Z0-9._-]", "_");
            if (!cleanCustomName.contains(".")) {
                cleanCustomName += fileExtension;
            }
            Path filePath = Paths.get(uploadPath, cleanCustomName);
            Files.copy(file.getInputStream(), filePath);
            String imageUrl = "/images/" + cleanCustomName;
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> deleteImage(String filename) {
        try {
            Path filePath = Paths.get(uploadPath, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return ResponseEntity.ok("File deleted successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<String> deleteImageByUrl(String imageUrl) {
        if (imageUrl != null && imageUrl.startsWith("/images/")) {
            try {
                Path path = Paths.get(uploadPath, imageUrl.replace("/images/", ""));
                Files.deleteIfExists(path);
                return ResponseEntity.ok("Image deleted");
            } catch (IOException e) {
                return ResponseEntity.internalServerError().body("Delete failed: " + e.getMessage());
            }
        }
        return ResponseEntity.badRequest().body("Invalid imageUrl");
    }
} 