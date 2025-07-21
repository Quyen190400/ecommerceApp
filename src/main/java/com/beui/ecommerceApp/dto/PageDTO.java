package com.beui.ecommerceApp.dto;

import java.util.List;

public class PageDTO<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;

    public PageDTO(org.springframework.data.domain.Page<T> page) {
        this.content = page.getContent();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
        this.number = page.getNumber();
        this.size = page.getSize();
    }

    public List<T> getContent() { return content; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
    public int getNumber() { return number; }
    public int getSize() { return size; }
} 