package com.beui.ecommerceApp.dto;

import java.math.BigDecimal;

public class ProductInfo {
    private Long id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private String description;
    private String origin;
    private String teaType;
    private String tasteNote;
    private String healthBenefit;
    private String usageGuide;
    private Integer stockQuantity;
    private Integer soldCount;
    
    // Constructors
    public ProductInfo() {}
    
    public ProductInfo(Long id, String name, String imageUrl, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
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
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
} 