package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.dto.AdminProductRequest;
import com.beui.ecommerceApp.dto.AdminProductResponse;
import com.beui.ecommerceApp.dto.PageDTO;
import com.beui.ecommerceApp.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin Product", description = "Admin APIs for managing products: create, update, delete, and toggle status.")
@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    @Autowired
    private ProductService productService;

    @Operation(description = "Get a paginated list of all products for admin (requires ADMIN role)")
    @GetMapping
    public ResponseEntity<PageDTO<AdminProductResponse>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(new PageDTO<>(productService.getAllAdminProducts(PageRequest.of(page, size))));
    }

    @Operation(description = "Create a new product (requires ADMIN role)")
    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam("price") Double price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("teaType") String teaType,
            @RequestParam(value = "tasteNote", required = false) String tasteNote,
            @RequestParam(value = "usageGuide", required = false) String usageGuide,
            @RequestParam(value = "healthBenefit", required = false) String healthBenefit,
            @RequestParam(value = "origin", required = false) String origin,
            @RequestParam(value = "status", required = false, defaultValue = "true") Boolean status,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        try {
            return ResponseEntity.ok(productService.createAdminProduct(
                name, description, imageUrl, price, stockQuantity, teaType, tasteNote, usageGuide, healthBenefit, origin, status, file
            ));
        } catch (IOException ex) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", "Lỗi xử lý file ảnh: " + ex.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (RuntimeException ex) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // PUT: Update sản phẩm KHÔNG có file (application/json)
    @Operation(description = "Update a product by ID with JSON body (requires ADMIN role)")
    @PutMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<?> updateProductJson(
            @PathVariable("id") Long id,
            @RequestBody AdminProductRequest req
    ) {
        try {
            return ResponseEntity.ok(productService.updateAdminProduct(id, req, null));
        } catch (IOException ex) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", "Lỗi xử lý file ảnh: " + ex.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (RuntimeException ex) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // PUT: Update sản phẩm CÓ file (multipart/form-data)
    @Operation(description = "Update a product by ID with multipart form data (requires ADMIN role)")
    @PutMapping(path = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateProductMultipart(
            @PathVariable("id") Long id,
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("imageUrl") String imageUrl,
            @RequestParam("price") Double price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("teaType") String teaType,
            @RequestParam(value = "tasteNote", required = false) String tasteNote,
            @RequestParam(value = "usageGuide", required = false) String usageGuide,
            @RequestParam(value = "healthBenefit", required = false) String healthBenefit,
            @RequestParam(value = "origin", required = false) String origin,
            @RequestParam(value = "status", required = false, defaultValue = "true") Boolean status,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        try {
            return ResponseEntity.ok(productService.updateAdminProduct(
                id,
                name, description, imageUrl, price, stockQuantity, teaType, tasteNote, usageGuide, healthBenefit, origin, status, file
            ));
        } catch (IOException ex) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", "Lỗi xử lý file ảnh: " + ex.getMessage());
            return ResponseEntity.status(500).body(error);
        } catch (RuntimeException ex) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @Operation(description = "Delete a product by ID (requires ADMIN role)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteAdminProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Toggle the status (active/inactive) of a product by ID (requires ADMIN role)")
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<AdminProductResponse> toggleProductStatus(@PathVariable("id") Long id) {
        return ResponseEntity.ok(productService.toggleAdminProductStatus(id));
    }
} 