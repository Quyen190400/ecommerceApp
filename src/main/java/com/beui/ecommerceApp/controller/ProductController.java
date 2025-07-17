package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.Product;
import com.beui.ecommerceApp.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Tag(name = "Product", description = "APIs for viewing and managing tea products.")
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