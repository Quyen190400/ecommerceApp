package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "File Upload", description = "API for uploading product images and files.")
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${app.upload.path:uploads/images}")
    private String uploadPath;

    @Autowired
    private FileUploadService fileUploadService;

    @Operation(description = "Upload an image file and return its URL (requires authentication)")
    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.uploadImage(file);
        return ResponseEntity.ok(url);
    }
} 