package com.beui.ecommerceApp.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class AdminProductResponse {
    private Long id;
    private String name;
    private String description;
    private String teaType;
    private String origin;
    private Double price;
    private Integer stockQuantity;
    private Integer soldCount;
    private String imageUrl;
    private String tasteNote;
    private String healthBenefit;
    private String usageGuide;
    private Boolean status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // getters and setters
} 