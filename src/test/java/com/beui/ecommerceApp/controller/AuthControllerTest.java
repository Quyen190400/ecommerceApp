package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.dto.LoginRequest;
import com.beui.ecommerceApp.dto.LoginResponse;
import com.beui.ecommerceApp.dto.RegisterRequest;
import com.beui.ecommerceApp.service.AuthService;
import com.beui.ecommerceApp.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {
    @Mock
    private AuthService authService;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(authService.login(any(), any())).thenReturn(ResponseEntity.ok(new LoginResponse()));
        ResponseEntity<LoginResponse> response = authController.login(req, resp);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetCurrentUser() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        Map<String, Object> map = new HashMap<>();
        when(authService.getCurrentUser(any())).thenReturn(ResponseEntity.ok(map));
        ResponseEntity<Map<String, Object>> response = authController.getCurrentUser(req);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testRegister() {
        RegisterRequest req = new RegisterRequest();
        Map<String, Object> map = new HashMap<>();
        when(authService.register(any())).thenReturn(ResponseEntity.ok(map));
        ResponseEntity<Map<String, Object>> response = authController.register(req);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testLogout() {
        HttpServletResponse resp = mock(HttpServletResponse.class);
        Map<String, String> map = new HashMap<>();
        when(authService.logout(any())).thenReturn(ResponseEntity.ok(map));
        ResponseEntity<Map<String, String>> response = authController.logout(resp);
        assertEquals(200, response.getStatusCodeValue());
    }
} 