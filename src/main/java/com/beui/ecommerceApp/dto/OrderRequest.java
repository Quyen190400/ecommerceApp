package com.beui.ecommerceApp.dto;

import java.util.List;

public class OrderRequest {
    private List<Long> selectedItemIds;
    private String shippingAddress;
    private String shippingMethod;
    private String phoneNumber;
    private String orderNotes;
    
    // Constructors
    public OrderRequest() {}
    
    public OrderRequest(List<Long> selectedItemIds, String shippingAddress, String shippingMethod, String phoneNumber, String orderNotes) {
        this.selectedItemIds = selectedItemIds;
        this.shippingAddress = shippingAddress;
        this.shippingMethod = shippingMethod;
        this.phoneNumber = phoneNumber;
        this.orderNotes = orderNotes;
    }
    
    // Getters and Setters
    public List<Long> getSelectedItemIds() {
        return selectedItemIds;
    }
    
    public void setSelectedItemIds(List<Long> selectedItemIds) {
        this.selectedItemIds = selectedItemIds;
    }
    
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getShippingMethod() {
        return shippingMethod;
    }
    
    public void setShippingMethod(String shippingMethod) {
        this.shippingMethod = shippingMethod;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getOrderNotes() {
        return orderNotes;
    }
    
    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }
} 