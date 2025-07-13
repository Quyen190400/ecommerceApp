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

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<PageDTO<AdminProductResponse>> getAllProducts(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(PageDTO.of(productService.getAllAdminProducts(PageRequest.of(page, size))));
    }

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
    ) throws IOException {
        try {
            // Lưu file nếu có
            if (file != null && !file.isEmpty()) {
                String uploadPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "images";
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) uploadDir.mkdirs();
                File dest = new File(uploadDir, imageUrl);
                file.transferTo(dest);
            }
            AdminProductRequest req = new AdminProductRequest();
            req.setName(name);
            req.setDescription(description);
            req.setImageUrl(imageUrl);
            req.setPrice(price);
            req.setStockQuantity(stockQuantity);
            req.setTeaType(teaType);
            req.setTasteNote(tasteNote);
            req.setUsageGuide(usageGuide);
            req.setHealthBenefit(healthBenefit);
            req.setOrigin(origin);
            req.setStatus(status);
            return ResponseEntity.ok(productService.createAdminProduct(req));
        } catch (RuntimeException ex) {
            java.util.Map<String, String> error = new java.util.HashMap<>();
            error.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<AdminProductResponse> updateProductJson(
            @PathVariable("id") Long id,
            @RequestBody AdminProductRequest req
    ) {
        return ResponseEntity.ok(productService.updateAdminProduct(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteAdminProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<AdminProductResponse> toggleProductStatus(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleAdminProductStatus(id));
    }
} 