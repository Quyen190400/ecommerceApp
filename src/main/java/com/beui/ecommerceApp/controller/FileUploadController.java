package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.service.FileUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${app.upload.path:uploads/images}")
    private String uploadPath;

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "customName", required = false) String customName) {
        return fileUploadService.uploadImage(file, customName);
    }

    @PostMapping("/image/custom")
    public ResponseEntity<String> uploadImageWithCustomName(
            @RequestParam("file") MultipartFile file,
            @RequestParam("customName") String customName) {
        return fileUploadService.uploadImageWithCustomName(file, customName);
    }

    @DeleteMapping("/image/{filename}")
    public ResponseEntity<String> deleteImage(@PathVariable String filename) {
        return fileUploadService.deleteImage(filename);
    }

    @DeleteMapping("/image/url")
    public ResponseEntity<String> deleteImageByUrl(@RequestParam("imageUrl") String imageUrl) {
        return fileUploadService.deleteImageByUrl(imageUrl);
    }
} 