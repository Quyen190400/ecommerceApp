package com.beui.ecommerceApp.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    ResponseEntity<String> uploadImage(MultipartFile file, String customName);
    ResponseEntity<String> uploadImageWithCustomName(MultipartFile file, String customName);
    ResponseEntity<String> deleteImage(String filename);
    ResponseEntity<String> deleteImageByUrl(String imageUrl);
} 