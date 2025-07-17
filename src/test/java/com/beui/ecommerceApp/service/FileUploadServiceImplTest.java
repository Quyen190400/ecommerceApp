package com.beui.ecommerceApp.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileUploadServiceImplTest {
    @Mock
    private Cloudinary cloudinary;
    @Mock
    private MultipartFile multipartFile;
    @InjectMocks
    private FileUploadServiceImpl fileUploadService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadImage_success() throws Exception {
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(multipartFile.getBytes()).thenReturn(new byte[]{1,2,3});
        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "http://image.url");
        when(uploader.upload(any(), anyMap())).thenReturn(uploadResult);
        String url = fileUploadService.uploadImage(multipartFile);
        assertEquals("http://image.url", url);
    }

    @Test
    void uploadImage_throwsIOException() throws Exception {
        when(multipartFile.getBytes()).thenThrow(new IOException("fail"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> fileUploadService.uploadImage(multipartFile));
        assertTrue(ex.getMessage().contains("Upload ảnh thất bại"));
    }

    @Test
    void uploadImage_throwsCloudinaryIOException() throws Exception {
        Uploader uploader = mock(Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(multipartFile.getBytes()).thenReturn(new byte[]{1,2,3});
        when(uploader.upload(any(), anyMap())).thenThrow(new IOException("fail"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> fileUploadService.uploadImage(multipartFile));
        assertTrue(ex.getMessage().contains("Upload ảnh thất bại"));
    }
} 