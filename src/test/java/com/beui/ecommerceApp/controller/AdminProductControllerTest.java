package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.dto.AdminProductRequest;
import com.beui.ecommerceApp.dto.AdminProductResponse;
import com.beui.ecommerceApp.dto.PageDTO;
import com.beui.ecommerceApp.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyDouble;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.when;

class AdminProductControllerTest {
    @Mock
    private ProductService productService;
    @InjectMocks
    private AdminProductController adminProductController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProducts() {
        when(productService.getAllAdminProducts(any())).thenReturn(new PageImpl<>(Collections.emptyList()));
        ResponseEntity<PageDTO<AdminProductResponse>> response = adminProductController.getAllProducts(0, 10);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testCreateProductSuccess() throws IOException {
        when(productService.createAdminProduct(anyString(), any(), anyString(), anyDouble(), anyInt(), anyString(), any(), any(), any(), any(), anyBoolean(), any())).thenReturn(new AdminProductResponse());
        ResponseEntity<?> response = adminProductController.createProduct("name", "desc", "img", 1.0, 1, "tea", "note", "guide", "benefit", "origin", true, null);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testCreateProductIOException() throws IOException {
        when(productService.createAdminProduct(anyString(), any(), anyString(), anyDouble(), anyInt(), anyString(), any(), any(), any(), any(), anyBoolean(), any())).thenThrow(new IOException("IO error"));
        ResponseEntity<?> response = adminProductController.createProduct("name", "desc", "img", 1.0, 1, "tea", "note", "guide", "benefit", "origin", true, null);
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testCreateProductRuntimeException() throws IOException {
        when(productService.createAdminProduct(anyString(), any(), anyString(), anyDouble(), anyInt(), anyString(), any(), any(), any(), any(), anyBoolean(), any())).thenThrow(new RuntimeException("Runtime error"));
        ResponseEntity<?> response = adminProductController.createProduct("name", "desc", "img", 1.0, 1, "tea", "note", "guide", "benefit", "origin", true, null);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testUpdateProductJsonSuccess() throws IOException {
        when(productService.updateAdminProduct(anyLong(), any(AdminProductRequest.class), isNull())).thenReturn(new AdminProductResponse());
        ResponseEntity<?> response = adminProductController.updateProductJson(1L, new AdminProductRequest());
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testUpdateProductJsonIOException() throws IOException {
        when(productService.updateAdminProduct(anyLong(), any(AdminProductRequest.class), isNull())).thenThrow(new IOException("IO error"));
        ResponseEntity<?> response = adminProductController.updateProductJson(1L, new AdminProductRequest());
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testUpdateProductJsonRuntimeException() throws IOException {
        when(productService.updateAdminProduct(anyLong(), any(AdminProductRequest.class), isNull())).thenThrow(new RuntimeException("Runtime error"));
        ResponseEntity<?> response = adminProductController.updateProductJson(1L, new AdminProductRequest());
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testUpdateProductMultipartSuccess() throws IOException {
        when(productService.updateAdminProduct(anyLong(), anyString(), any(), anyString(), anyDouble(), anyInt(), anyString(), any(), any(), any(), any(), anyBoolean(), any())).thenReturn(new AdminProductResponse());
        ResponseEntity<?> response = adminProductController.updateProductMultipart(1L, "name", "desc", "img", 1.0, 1, "tea", "note", "guide", "benefit", "origin", true, null);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testUpdateProductMultipartIOException() throws IOException {
        when(productService.updateAdminProduct(anyLong(), anyString(), any(), anyString(), anyDouble(), anyInt(), anyString(), any(), any(), any(), any(), anyBoolean(), any())).thenThrow(new IOException("IO error"));
        ResponseEntity<?> response = adminProductController.updateProductMultipart(1L, "name", "desc", "img", 1.0, 1, "tea", "note", "guide", "benefit", "origin", true, null);
        assertEquals(500, response.getStatusCodeValue());
    }

    @Test
    void testUpdateProductMultipartRuntimeException() throws IOException {
        when(productService.updateAdminProduct(anyLong(), anyString(), any(), anyString(), anyDouble(), anyInt(), anyString(), any(), any(), any(), any(), anyBoolean(), any())).thenThrow(new RuntimeException("Runtime error"));
        ResponseEntity<?> response = adminProductController.updateProductMultipart(1L, "name", "desc", "img", 1.0, 1, "tea", "note", "guide", "benefit", "origin", true, null);
        assertEquals(400, response.getStatusCodeValue());
    }

    @Test
    void testDeleteProduct() {
        doNothing().when(productService).deleteAdminProduct(anyLong());
        ResponseEntity<Void> response = adminProductController.deleteProduct(1L);
        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    void testToggleProductStatus() {
        when(productService.toggleAdminProductStatus(anyLong())).thenReturn(new AdminProductResponse());
        ResponseEntity<AdminProductResponse> response = adminProductController.toggleProductStatus(1L);
        assertEquals(200, response.getStatusCodeValue());
    }
} 