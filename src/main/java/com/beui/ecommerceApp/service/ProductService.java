package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.dto.AdminProductRequest;
import com.beui.ecommerceApp.dto.AdminProductResponse;
import com.beui.ecommerceApp.entity.Product;
import com.beui.ecommerceApp.repository.ProductRepository;
import com.beui.ecommerceApp.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // Lấy tất cả sản phẩm
    public List<Product> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .filter(product -> product.getStatus() != null && product.getStatus())
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Lấy sản phẩm theo ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }
    
    // Lấy sản phẩm theo danh mục
    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
    
    // Tìm kiếm sản phẩm theo tên
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByKeyword(name, null).getContent();
    }
    
    // Lấy sản phẩm còn hàng
    public List<Product> getAvailableProducts() {
        List<Product> products = productRepository.findAvailableProducts();
        return products.stream()
                .filter(product -> product.getStatus() != null && product.getStatus())
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Thêm sản phẩm mới
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    
    // Cập nhật sản phẩm
    public Product updateProduct(Long id, Product productDetails) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setImageUrl(productDetails.getImageUrl());
            product.setStockQuantity(productDetails.getStockQuantity());
            product.setCategory(productDetails.getCategory());
            return productRepository.save(product);
        }
        return null;
    }
    
    // Xóa sản phẩm
    public boolean deleteProduct(Long id) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // Cập nhật số lượng tồn kho
    public boolean updateStockQuantity(Long productId, int quantity) {
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            product.setStockQuantity(quantity);
            productRepository.save(product);
            return true;
        }
        return false;
    }
    
    // Create mock product data for testing
    public Product createMockProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Trà xanh Thái Nguyên đặc biệt");
        product.setDescription("Trà xanh Thái Nguyên được hái từ những lá trà non nhất, chế biến theo phương pháp truyền thống, giữ nguyên hương vị đặc trưng của vùng đất Thái Nguyên.");
        product.setPrice(new BigDecimal("450000"));
        product.setImageUrl("tra-thai-nguyen.jpg");
        product.setOrigin("Thái Nguyên, Việt Nam");
        product.setTeaType("GREEN_TEA");
        product.setTasteNote("Đậm đà, hậu ngọt sâu lắng, hương thơm tự nhiên");
        product.setHealthBenefit("Giúp giảm stress và lo âu, Tăng cường hệ miễn dịch, Chống oxy hóa, Hỗ trợ giảm cân, Tốt cho tim mạch");
        product.setUsageGuide("1. Rửa ấm chén bằng nước nóng\n2. Cho 3-5g trà vào ấm\n3. Rót nước sôi 80-85°C\n4. Ủ 2-3 phút\n5. Thưởng thức");
        product.setStockQuantity(50);
        return product;
    }
    
    public List<Product> getBestSellers() {
        List<Product> products = productRepository.findTop4ByOrderBySoldCountDesc();
        return products.stream()
                .filter(product -> product.getStatus() != null && product.getStatus())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Product> getProductsByBenefit(String benefit) {
        Map<String, List<String>> benefitKeywords = Map.of(
            "an-than", List.of("an thần", "thư giản", "stress", "ngủ ngon"),
            "giam-can", List.of("giảm cân", "giam can", "weight loss"),
            "thao-moc", List.of("ngủ ngon", "sleep", "an thần"),
            "dung-cu", List.of("giữ nhiệt")
        );
        List<String> keywords = benefitKeywords.getOrDefault(benefit, List.of(benefit));
        java.util.Set<Product> result = new java.util.HashSet<>();
        for (String keyword : keywords) {
            result.addAll(productRepository.findByHealthBenefitContainingIgnoreCase(keyword));
        }
        return new java.util.ArrayList<>(result).stream()
                .filter(product -> product.getStatus() != null && product.getStatus())
                .collect(java.util.stream.Collectors.toList());
    }

    // ADMIN: Lấy danh sách sản phẩm (phân trang)
    public Page<AdminProductResponse> getAllAdminProducts(Pageable pageable) {
        Page<Product> page = productRepository.findAll(pageable);
        List<AdminProductResponse> content = page.getContent().stream().map(this::toAdminProductResponse).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    // ADMIN: Thêm sản phẩm
    public AdminProductResponse createAdminProduct(AdminProductRequest req) {
        // Kiểm tra trùng tên sản phẩm
        if (productRepository.existsByName(req.getName())) {
            throw new RuntimeException("Tên sản phẩm đã tồn tại!");
        }
        Product product = new Product();
        mapAdminProductRequestToProduct(req, product);
        product.setCreatedAt(java.time.LocalDateTime.now());
        product.setUpdatedAt(java.time.LocalDateTime.now());
        Product saved = productRepository.save(product);
        return toAdminProductResponse(saved);
    }

    // ADMIN: Cập nhật sản phẩm
    public AdminProductResponse updateAdminProduct(Long id, AdminProductRequest req) {
        Product product = productRepository.findById(id).orElseThrow();
        // Kiểm tra tên sản phẩm trùng lặp khi update
        String newName = req.getName();
        String oldName = product.getName();
        // Nếu tên mới khác với tên cũ và tên mới đã tồn tại
        if (!newName.equals(oldName) && productRepository.existsByName(newName)) {
            throw new RuntimeException("Tên sản phẩm '" + newName + "' đã tồn tại!");
        }
        // Nếu không truyền imageUrl (null hoặc rỗng), giữ nguyên ảnh cũ
        String oldImageUrl = product.getImageUrl();
        if (req.getImageUrl() == null || req.getImageUrl().trim().isEmpty()) {
            req.setImageUrl(oldImageUrl);
        }
        mapAdminProductRequestToProduct(req, product);
        product.setUpdatedAt(java.time.LocalDateTime.now());
        Product saved = productRepository.save(product);
        return toAdminProductResponse(saved);
    }

    // ADMIN: Thêm sản phẩm (overload, nhận từng trường và file)
    public AdminProductResponse createAdminProduct(
            String name,
            String description,
            String imageUrl,
            Double price,
            Integer stockQuantity,
            String teaType,
            String tasteNote,
            String usageGuide,
            String healthBenefit,
            String origin,
            Boolean status,
            org.springframework.web.multipart.MultipartFile file
    ) throws java.io.IOException {
        // Lưu file nếu có
        if (file != null && !file.isEmpty()) {
            String uploadPath = System.getProperty("user.dir") + java.io.File.separator + "uploads" + java.io.File.separator + "images";
            java.io.File uploadDir = new java.io.File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();
            java.io.File dest = new java.io.File(uploadDir, imageUrl);
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
        return createAdminProduct(req);
    }

    // ADMIN: Update sản phẩm (overload, nhận từng trường và file)
    public AdminProductResponse updateAdminProduct(
            Long id,
            String name,
            String description,
            String imageUrl,
            Double price,
            Integer stockQuantity,
            String teaType,
            String tasteNote,
            String usageGuide,
            String healthBenefit,
            String origin,
            Boolean status,
            org.springframework.web.multipart.MultipartFile file
    ) throws java.io.IOException {
        // Lưu file nếu có
        if (file != null && !file.isEmpty()) {
            String uploadPath = System.getProperty("user.dir") + java.io.File.separator + "uploads" + java.io.File.separator + "images";
            java.io.File uploadDir = new java.io.File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();
            java.io.File dest = new java.io.File(uploadDir, imageUrl);
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
        return updateAdminProduct(id, req);
    }

    // ADMIN: Update sản phẩm (overload, nhận thêm file=null cho JSON)
    public AdminProductResponse updateAdminProduct(Long id, AdminProductRequest req, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        // Nếu có file thì xử lý lưu file và cập nhật imageUrl
        if (file != null && !file.isEmpty() && req.getImageUrl() != null && !req.getImageUrl().trim().isEmpty()) {
            String uploadPath = System.getProperty("user.dir") + java.io.File.separator + "uploads" + java.io.File.separator + "images";
            java.io.File uploadDir = new java.io.File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs();
            java.io.File dest = new java.io.File(uploadDir, req.getImageUrl());
            file.transferTo(dest);
        }
        return updateAdminProduct(id, req);
    }

    // ADMIN: Xóa sản phẩm
    public void deleteAdminProduct(Long id) {
        productRepository.deleteById(id);
    }

    // ADMIN: Bật/tắt trạng thái hoạt động
    public AdminProductResponse toggleAdminProductStatus(Long id) {
        Product product = productRepository.findById(id).orElseThrow();
        Boolean current = product.getStatus() != null ? product.getStatus() : true;
        product.setStatus(!current);
        product.setUpdatedAt(java.time.LocalDateTime.now());
        Product saved = productRepository.save(product);
        return toAdminProductResponse(saved);
    }

    // Helper: Map request -> entity
    private void mapAdminProductRequestToProduct(AdminProductRequest req, Product product) {
        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setTeaType(req.getTeaType());
        product.setOrigin(req.getOrigin());
        product.setPrice(req.getPrice() != null ? java.math.BigDecimal.valueOf(req.getPrice()) : null);
        product.setStockQuantity(req.getStockQuantity());
        product.setImageUrl(req.getImageUrl());
        product.setTasteNote(req.getTasteNote());
        product.setHealthBenefit(req.getHealthBenefit());
        product.setUsageGuide(req.getUsageGuide());
        product.setStatus(req.getStatus() != null ? req.getStatus() : true);
        // Gán category theo teaType (category name)
        if (req.getTeaType() != null) {
            categoryRepository.findByName(req.getTeaType()).ifPresent(product::setCategory);
        }
    }

    // Helper: Map entity -> response
    private AdminProductResponse toAdminProductResponse(Product p) {
        AdminProductResponse res = new AdminProductResponse();
        res.setId(p.getId());
        res.setName(p.getName());
        res.setDescription(p.getDescription());
        res.setTeaType(p.getTeaType());
        res.setOrigin(p.getOrigin());
        res.setPrice(p.getPrice() != null ? p.getPrice().doubleValue() : null);
        res.setStockQuantity(p.getStockQuantity());
        res.setSoldCount(p.getSoldCount());
        res.setImageUrl(p.getImageUrl());
        res.setTasteNote(p.getTasteNote());
        res.setHealthBenefit(p.getHealthBenefit());
        res.setUsageGuide(p.getUsageGuide());
        res.setStatus(p.getStatus() != null ? p.getStatus() : true);
        res.setCreatedAt(p.getCreatedAt());
        res.setUpdatedAt(p.getUpdatedAt());
        return res;
    }
} 