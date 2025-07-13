package com.beui.ecommerceApp.dto;

import lombok.Data;

@Data
public class AdminProductRequest {
    private String name;
    private String description;
    private String teaType;
    private String origin;
    private Double price;
    private Integer stockQuantity;
    private String imageUrl;
    private String tasteNote;
    private String healthBenefit;
    private String usageGuide;
    private Boolean status;
    // getters and setters
} 