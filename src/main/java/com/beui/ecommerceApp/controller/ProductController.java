package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.Product;
import com.beui.ecommerceApp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    @Operation(description = "Get all available products (stock > 0)")
    public List<Product> getAll() {
        return productService.getAvailableProducts(); 
    }

    @GetMapping("/all")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Get all products including out of stock (requires ADMIN role)")
    public List<Product> getAllForAdmin() { 
        return productService.getAllProducts(); 
    }

    @GetMapping("/{id}")
    @Operation(description = "Get product by ID")
    public ResponseEntity<Product> getById(@PathVariable("id") Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/detail/{id}")
    @Operation(description = "Get product detail page by ID")
    public ResponseEntity<Product> getProductDetail(@PathVariable("id") Long id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(description = "Get products by category")
    public List<Product> getByCategory(@PathVariable("categoryId") Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @GetMapping("/search")
    @Operation(description = "Search products by name")
    public List<Product> searchProducts(@RequestParam("name") String name) {
        return productService.searchProductsByName(name);
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Create new product (requires ADMIN role)")
    public Product create(@RequestBody Product product) { 
        return productService.createProduct(product); 
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Update product by ID (requires ADMIN role)")
    public ResponseEntity<Product> update(@PathVariable("id") Long id, @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        return updatedProduct != null ? ResponseEntity.ok(updatedProduct) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/stock")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Update product stock by ID (requires ADMIN role)")
    public ResponseEntity<String> updateStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity) {
        boolean updated = productService.updateStockQuantity(id, quantity);
        return updated ? ResponseEntity.ok("Stock updated successfully") : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Delete product by ID (requires ADMIN role)")
    public ResponseEntity<String> delete(@PathVariable("id") Long id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? ResponseEntity.ok("Product deleted successfully") : ResponseEntity.notFound().build();
    }

    @PutMapping("/admin/update-image-paths")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Update all product image URLs to use /images/ folder (requires ADMIN role)")
    public ResponseEntity<String> updateImagePaths() {
        List<Product> products = productService.getAllProducts();
        int updatedCount = 0;
        
        for (Product product : products) {
            String currentImageUrl = product.getImageUrl();
            if (currentImageUrl != null && !currentImageUrl.isEmpty() && !currentImageUrl.startsWith("/images/")) {
                // Extract filename from current URL
                String filename = currentImageUrl.substring(currentImageUrl.lastIndexOf("/") + 1);
                // Update to new path
                product.setImageUrl("/images/" + filename);
                productService.updateProduct(product.getId(), product);
                updatedCount++;
            }
        }
        
        return ResponseEntity.ok("Updated " + updatedCount + " product image paths to /images/ folder");
    }

    @GetMapping("/debug/info")
    @Operation(description = "Debug endpoint to check database status")
    public ResponseEntity<String> debugInfo() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Product Database Debug ===\n");
            
            List<Product> allProducts = productService.getAllProducts();
            long totalProducts = allProducts.size();
            sb.append("Total products: " + totalProducts + "\n");
            
            if (totalProducts > 0) {
                sb.append("\n=== Sample Products ===\n");
                allProducts.stream().limit(5).forEach(product -> {
                    sb.append(String.format("ID: %d, Name: %s, Price: %s, Stock: %d\n", 
                        product.getId(), product.getName(), product.getPrice(), product.getStockQuantity()));
                });
            } else {
                sb.append("\nNo products found in database\n");
            }
            
            return ResponseEntity.ok(sb.toString());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Database error: " + e.getMessage());
        }
    }

    @PostMapping("/debug/create-sample")
    @Operation(description = "Create sample products for testing")
    public ResponseEntity<String> createSampleProducts() {
        try {
            // Check if products already exist
            List<Product> existingProducts = productService.getAllProducts();
            if (!existingProducts.isEmpty()) {
                return ResponseEntity.ok("Products already exist in database");
            }
            
            // Create sample products
            Product product1 = new Product();
            product1.setName("Sample Product 1");
            product1.setDescription("This is a sample product for testing");
            product1.setPrice(new BigDecimal("99.99"));
            product1.setStockQuantity(10);
            product1.setImageUrl("/images/product-placeholder.jpg");
            productService.createProduct(product1);
            
            Product product2 = new Product();
            product2.setName("Sample Product 2");
            product2.setDescription("Another sample product");
            product2.setPrice(new BigDecimal("149.99"));
            product2.setStockQuantity(5);
            product2.setImageUrl("/images/product-placeholder.jpg");
            productService.createProduct(product2);
            
            Product product3 = new Product();
            product3.setName("Out of Stock Product");
            product3.setDescription("This product has no stock");
            product3.setPrice(new BigDecimal("199.99"));
            product3.setStockQuantity(0);
            product3.setImageUrl("/images/product-placeholder.jpg");
            productService.createProduct(product3);
            
            return ResponseEntity.ok("Created 3 sample products successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error creating samples: " + e.getMessage());
        }
    }

    @GetMapping("/api/products")
    @Operation(description = "Get all products for frontend display")
    public ResponseEntity<List<Product>> getAllProductsForFrontend() {
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/best-sellers")
    public List<Product> getBestSellers() {
        return productService.getBestSellers();
    }

    @GetMapping("/benefit/{benefit}")
    public ResponseEntity<List<Product>> getProductsByBenefit(@PathVariable("benefit") String benefit) {
        List<Product> products = productService.getProductsByBenefit(benefit);
        return ResponseEntity.ok(products);
    }
}