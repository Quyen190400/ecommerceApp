package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.entity.Category;
import com.beui.ecommerceApp.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllCategories() {
        List<Category> categories = Arrays.asList(new Category(), new Category());
        when(categoryRepository.findAll()).thenReturn(categories);
        assertEquals(2, categoryService.getAllCategories().size());
    }

    @Test
    void getCategoryById_found() {
        Category category = new Category();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        assertTrue(categoryService.getCategoryById(1L).isPresent());
    }

    @Test
    void getCategoryById_notFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertFalse(categoryService.getCategoryById(1L).isPresent());
    }

    @Test
    void getCategoryByName_found() {
        Category category = new Category();
        when(categoryRepository.findByName("Tea")).thenReturn(Optional.of(category));
        assertTrue(categoryService.getCategoryByName("Tea").isPresent());
    }

    @Test
    void getCategoryByName_notFound() {
        when(categoryRepository.findByName("Tea")).thenReturn(Optional.empty());
        assertFalse(categoryService.getCategoryByName("Tea").isPresent());
    }

    @Test
    void createCategory() {
        Category category = new Category();
        when(categoryRepository.save(category)).thenReturn(category);
        assertEquals(category, categoryService.createCategory(category));
    }

    @Test
    void updateCategory_found() {
        Category oldCat = new Category();
        oldCat.setId(1L);
        oldCat.setName("Old");
        oldCat.setDescription("desc");
        Category newCat = new Category();
        newCat.setName("New");
        newCat.setDescription("newdesc");
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(oldCat));
        when(categoryRepository.save(any(Category.class))).thenReturn(oldCat);
        Category result = categoryService.updateCategory(1L, newCat);
        assertEquals("New", result.getName());
        assertEquals("newdesc", result.getDescription());
    }

    @Test
    void updateCategory_notFound() {
        Category newCat = new Category();
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertNull(categoryService.updateCategory(1L, newCat));
    }

    @Test
    void deleteCategory_found() {
        Category cat = new Category();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        doNothing().when(categoryRepository).deleteById(1L);
        assertTrue(categoryService.deleteCategory(1L));
    }

    @Test
    void deleteCategory_notFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertFalse(categoryService.deleteCategory(1L));
    }
} 