package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.Category;
import com.beui.ecommerceApp.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

class CategoryControllerTest {
    @Mock
    private CategoryService categoryService;
    @InjectMocks
    private CategoryController categoryController;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void testGetAll() {
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());
        var response = categoryController.getAll();
        assertNotNull(response);
    }

    @Test
    void testGetById_Found() {
        Category cat = new Category();
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.of(cat));
        var response = categoryController.getById(1L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetById_NotFound() {
        when(categoryService.getCategoryById(1L)).thenReturn(Optional.empty());
        var response = categoryController.getById(1L);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetByName_Found() {
        Category cat = new Category();
        when(categoryService.getCategoryByName("abc")).thenReturn(Optional.of(cat));
        var response = categoryController.getByName("abc");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetByName_NotFound() {
        when(categoryService.getCategoryByName("abc")).thenReturn(Optional.empty());
        var response = categoryController.getByName("abc");
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testCreate() {
        Category cat = new Category();
        when(categoryService.createCategory(any())).thenReturn(cat);
        var response = categoryController.create(cat);
        assertNotNull(response);
    }

    @Test
    void testUpdate_Found() {
        Category cat = new Category();
        when(categoryService.updateCategory(eq(1L), any())).thenReturn(cat);
        var response = categoryController.update(1L, cat);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testUpdate_NotFound() {
        when(categoryService.updateCategory(eq(1L), any())).thenReturn(null);
        var response = categoryController.update(1L, new Category());
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testDelete_Success() {
        when(categoryService.deleteCategory(1L)).thenReturn(true);
        var response = categoryController.delete(1L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDelete_NotFound() {
        when(categoryService.deleteCategory(1L)).thenReturn(false);
        var response = categoryController.delete(1L);
        assertEquals(404, response.getStatusCodeValue());
    }
} 