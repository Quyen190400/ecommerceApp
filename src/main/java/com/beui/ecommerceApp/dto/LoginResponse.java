package com.beui.ecommerceApp.dto;

public class LoginResponse {
    private String token;
    private String message;
    private String username;
    private String role;
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }
    
    public LoginResponse(String token, String message, String username, String role) {
        this.token = token;
        this.message = message;
        this.username = username;
        this.role = role;
    }
    
    // Getters and Setters
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
} 