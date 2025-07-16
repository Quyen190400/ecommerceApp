package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.dto.LoginRequest;
import com.beui.ecommerceApp.dto.LoginResponse;
import com.beui.ecommerceApp.dto.RegisterRequest;
import com.beui.ecommerceApp.service.AuthService;
import com.beui.ecommerceApp.service.JwtService;
import com.beui.ecommerceApp.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication", description = "APIs for user authentication: login, register, logout, and get current user info.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private AppUserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private AuthService authService;
    
    @Operation(description = "Authenticate user and return JWT token (login)")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        return authService.login(loginRequest, response);
    }
    
    @Operation(description = "Get current authenticated user information from JWT token")
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        return authService.getCurrentUser(request);
    }
    
    @Operation(description = "Register a new user account")
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }
    
    @Operation(description = "Logout user and clear JWT cookie")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        return authService.logout(response);
    }
    

}