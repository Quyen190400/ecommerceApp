package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.dto.LoginRequest;
import com.beui.ecommerceApp.dto.LoginResponse;
import com.beui.ecommerceApp.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface AuthService {
    ResponseEntity<LoginResponse> login(LoginRequest loginRequest, HttpServletResponse response);
    ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request);
    ResponseEntity<Map<String, Object>> register(RegisterRequest registerRequest);
    ResponseEntity<Map<String, String>> logout(HttpServletResponse response);
    String webRegister(String fullName, String email, String password, String confirmPassword, String agreeTerms, RedirectAttributes redirectAttributes);
} 