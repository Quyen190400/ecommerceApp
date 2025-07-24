package com.beui.ecommerceApp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.persistence.Transient;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "product")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 255)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "image_url", length = 255)
    private String imageUrl;
    
    @Column(name = "origin", length = 255)
    private String origin;
    
    @Column(name = "tea_type", length = 255)
    private String teaType;
    
    @Column(name = "taste_note", columnDefinition = "TEXT")
    private String tasteNote;
    
    @Column(name = "health_benefit", columnDefinition = "TEXT")
    private String healthBenefit;
    
    @Column(name = "usage_guide", columnDefinition = "TEXT")
    private String usageGuide;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Column(name = "sold_count")
    private Integer soldCount = 0;
    
    @Column(name = "status")
    private Boolean status = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<OrderItem> orderItems;
    
    @Transient
    private List<String> usageGuideSteps;
    
    // Constructors
    public Product() {
        ZoneId vnZone = ZoneId.of("Asia/Ho_Chi_Minh");
        this.createdAt = LocalDateTime.now(vnZone);
        this.updatedAt = LocalDateTime.now(vnZone);
        this.stockQuantity = 0;
    }
    
    public Product(String name, String description, BigDecimal price, String imageUrl, 
                   String origin, String teaType, String tasteNote, String healthBenefit, 
                   String usageGuide, Category category, Integer stockQuantity) {
        this();
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.origin = origin;
        this.teaType = teaType;
        this.tasteNote = tasteNote;
        this.healthBenefit = healthBenefit;
        this.usageGuide = usageGuide;
        this.category = category;
        this.stockQuantity = stockQuantity;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getOrigin() {
        return origin;
    }
    
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    
    public String getTeaType() {
        return teaType;
    }
    
    public void setTeaType(String teaType) {
        this.teaType = teaType;
    }
    
    public String getTasteNote() {
        return tasteNote;
    }
    
    public void setTasteNote(String tasteNote) {
        this.tasteNote = tasteNote;
    }
    
    public String getHealthBenefit() {
        return healthBenefit;
    }
    
    public void setHealthBenefit(String healthBenefit) {
        this.healthBenefit = healthBenefit;
    }
    
    public String getUsageGuide() {
        return usageGuide;
    }
    
    public void setUsageGuide(String usageGuide) {
        this.usageGuide = usageGuide;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public Integer getSoldCount() {
        return soldCount;
    }
    
    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }
    
    public Boolean getStatus() {
        return status;
    }
    public void setStatus(Boolean status) {
        this.status = status;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
    
    public List<String> getUsageGuideSteps() {
        if (usageGuideSteps != null) return usageGuideSteps;
        usageGuideSteps = new ArrayList<>();
        if (usageGuide != null && !usageGuide.isBlank()) {
            // Tách theo số thứ tự (1. 2. ...)
            Pattern p = Pattern.compile("(\\d+\\.\\s*[^\\d]+)");
            Matcher m = p.matcher(usageGuide);
            while (m.find()) {
                String step = m.group(1).trim();
                // Loại bỏ số thứ tự ở đầu
                step = step.replaceFirst("^\\d+\\.\\s*", "").trim();
                if (!step.isEmpty()) usageGuideSteps.add(step);
            }
            // Nếu không match, fallback: tách theo dấu chấm
            if (usageGuideSteps.isEmpty()) {
                String[] parts = usageGuide.split("(?<=\\.)\\s*");
                for (String part : parts) {
                    String step = part.replaceFirst("^\\d+\\.\\s*", "").trim();
                    if (!step.isEmpty()) usageGuideSteps.add(step);
                }
            }
        }
        return usageGuideSteps;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
} 