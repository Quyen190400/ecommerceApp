package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.Product;
import com.beui.ecommerceApp.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class ProductControllerTest {
    @Mock private ProductService productService;
    @InjectMocks private ProductController productController;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void testGetAll() {
        when(productService.getAvailableProducts()).thenReturn(Collections.emptyList());
        var result = productController.getAll();
        assertNotNull(result);
    }

    @Test
    void testGetAllForAdmin() {
        when(productService.getAllProducts()).thenReturn(Collections.emptyList());
        var result = productController.getAllForAdmin();
        assertNotNull(result);
    }

    @Test
    void testGetById_Found() {
        Product p = new Product();
        when(productService.getProductById(1L)).thenReturn(Optional.of(p));
        ResponseEntity<Product> response = productController.getById(1L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetById_NotFound() {
        when(productService.getProductById(1L)).thenReturn(Optional.empty());
        ResponseEntity<Product> response = productController.getById(1L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetProductDetail_Found() {
        Product p = new Product();
        when(productService.getProductById(1L)).thenReturn(Optional.of(p));
        ResponseEntity<Product> response = productController.getProductDetail(1L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetProductDetail_NotFound() {
        when(productService.getProductById(1L)).thenReturn(Optional.empty());
        ResponseEntity<Product> response = productController.getProductDetail(1L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetByCategory() {
        when(productService.getProductsByCategory(1L)).thenReturn(Collections.emptyList());
        var result = productController.getByCategory(1L);
        assertNotNull(result);
    }

    @Test
    void testSearchProducts() {
        when(productService.searchProductsByName("abc")).thenReturn(Collections.emptyList());
        var result = productController.searchProducts("abc");
        assertNotNull(result);
    }
} 