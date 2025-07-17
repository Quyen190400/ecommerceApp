package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class FileUploadControllerTest {
    @Mock
    private FileUploadService fileUploadService;
    @InjectMocks
    private FileUploadController fileUploadController;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void testUploadImage_Success() {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[10]);
        when(fileUploadService.uploadImage(any())).thenReturn("url");
        ResponseEntity<?> response = fileUploadController.uploadImage(file);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testUploadImage_Fail() {
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[10]);
        when(fileUploadService.uploadImage(any())).thenThrow(new RuntimeException("fail"));
        assertThrows(RuntimeException.class, () -> fileUploadController.uploadImage(file));
    }
} 