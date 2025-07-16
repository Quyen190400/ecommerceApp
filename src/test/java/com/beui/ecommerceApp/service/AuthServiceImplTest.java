package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.dto.LoginRequest;
import com.beui.ecommerceApp.dto.LoginResponse;
import com.beui.ecommerceApp.dto.RegisterRequest;
import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.repository.AppUserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceImplTest {
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private AppUserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private RedirectAttributes redirectAttributes;
    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // login
    @Test
    void login_success() {
        LoginRequest req = new LoginRequest("test@email.com", "pass");
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(true);
        when(jwtService.generateToken(anyString())).thenReturn("token");
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setRole("CUSTOMER");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        ResponseEntity<LoginResponse> res = authService.login(req, httpServletResponse);
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("token", res.getBody().getToken());
        assertEquals("testuser", res.getBody().getUsername());
        assertEquals("CUSTOMER", res.getBody().getRole());
    }

    @Test
    void login_authenticationFails() {
        LoginRequest req = new LoginRequest("test@email.com", "pass");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new RuntimeException("fail"));
        ResponseEntity<LoginResponse> res = authService.login(req, httpServletResponse);
        assertEquals(400, res.getStatusCodeValue());
        assertEquals("Invalid email or password", res.getBody().getMessage());
    }

    @Test
    void login_notAuthenticated() {
        LoginRequest req = new LoginRequest("test@email.com", "pass");
        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(auth.isAuthenticated()).thenReturn(false);
        ResponseEntity<LoginResponse> res = authService.login(req, httpServletResponse);
        assertEquals(400, res.getStatusCodeValue());
        assertEquals("Authentication failed", res.getBody().getMessage());
    }

    // getCurrentUser
    @Test
    void getCurrentUser_success() {
        Cookie cookie = new Cookie("jwt_token", "token");
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtService.extractUsername("token")).thenReturn("test@email.com");
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@email.com");
        user.setFullName("Test User");
        user.setRole("CUSTOMER");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.of(user));
        ResponseEntity<Map<String, Object>> res = authService.getCurrentUser(httpServletRequest);
        assertEquals(200, res.getStatusCodeValue());
        assertTrue((Boolean) res.getBody().get("authenticated"));
        assertEquals(1L, res.getBody().get("id"));
        assertEquals("testuser", res.getBody().get("username"));
        assertEquals("test@email.com", res.getBody().get("email"));
        assertEquals("Test User", res.getBody().get("fullName"));
        assertEquals("CUSTOMER", res.getBody().get("role"));
    }

    @Test
    void getCurrentUser_noCookie() {
        when(httpServletRequest.getCookies()).thenReturn(null);
        ResponseEntity<Map<String, Object>> res = authService.getCurrentUser(httpServletRequest);
        assertEquals(401, res.getStatusCodeValue());
        assertFalse((Boolean) res.getBody().get("authenticated"));
        assertEquals("No JWT token found", res.getBody().get("message"));
    }

    @Test
    void getCurrentUser_invalidToken() {
        Cookie cookie = new Cookie("jwt_token", "token");
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtService.extractUsername("token")).thenReturn(null);
        ResponseEntity<Map<String, Object>> res = authService.getCurrentUser(httpServletRequest);
        assertEquals(401, res.getStatusCodeValue());
        assertFalse((Boolean) res.getBody().get("authenticated"));
        assertEquals("Invalid JWT token", res.getBody().get("message"));
    }

    @Test
    void getCurrentUser_userNotFound() {
        Cookie cookie = new Cookie("jwt_token", "token");
        when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtService.extractUsername("token")).thenReturn("test@email.com");
        when(userRepository.findByEmail("test@email.com")).thenReturn(Optional.empty());
        ResponseEntity<Map<String, Object>> res = authService.getCurrentUser(httpServletRequest);
        assertEquals(401, res.getStatusCodeValue());
        assertFalse((Boolean) res.getBody().get("authenticated"));
        assertEquals("User not found", res.getBody().get("message"));
    }

    @Test
    void getCurrentUser_exception() {
        when(httpServletRequest.getCookies()).thenThrow(new RuntimeException("fail"));
        ResponseEntity<Map<String, Object>> res = authService.getCurrentUser(httpServletRequest);
        assertEquals(401, res.getStatusCodeValue());
        assertFalse((Boolean) res.getBody().get("authenticated"));
        assertTrue(res.getBody().get("message").toString().contains("Error processing request"));
    }

    // register
    @Test
    void register_passwordsNotMatch() {
        RegisterRequest req = new RegisterRequest();
        req.setPassword("a");
        req.setConfirmPassword("b");
        ResponseEntity<Map<String, Object>> res = authService.register(req);
        assertEquals(400, res.getStatusCodeValue());
        assertFalse((Boolean) res.getBody().get("success"));
        assertEquals("Passwords do not match", res.getBody().get("message"));
    }

    @Test
    void register_emailExists() {
        RegisterRequest req = new RegisterRequest();
        req.setPassword("a");
        req.setConfirmPassword("a");
        req.setEmail("test@email.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        ResponseEntity<Map<String, Object>> res = authService.register(req);
        assertEquals(400, res.getStatusCodeValue());
        assertFalse((Boolean) res.getBody().get("success"));
        assertEquals("Email already exists", res.getBody().get("message"));
    }

    @Test
    void register_usernameExists() {
        RegisterRequest req = new RegisterRequest();
        req.setPassword("a");
        req.setConfirmPassword("a");
        req.setEmail("test@email.com");
        req.setFullName("Test User");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true, false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        ResponseEntity<Map<String, Object>> res = authService.register(req);
        assertEquals(200, res.getStatusCodeValue());
        assertTrue((Boolean) res.getBody().get("success"));
        assertEquals("User registered successfully", res.getBody().get("message"));
        assertEquals("test1", res.getBody().get("username"));
    }

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setPassword("a");
        req.setConfirmPassword("a");
        req.setEmail("test@email.com");
        req.setFullName("Test User");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        ResponseEntity<Map<String, Object>> res = authService.register(req);
        assertEquals(200, res.getStatusCodeValue());
        assertTrue((Boolean) res.getBody().get("success"));
        assertEquals("User registered successfully", res.getBody().get("message"));
        assertEquals("test", res.getBody().get("username"));
    }

    // logout
    @Test
    void logout_success() {
        ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
        ResponseEntity<Map<String, String>> res = authService.logout(httpServletResponse);
        verify(httpServletResponse).addCookie(captor.capture());
        Cookie cookie = captor.getValue();
        assertEquals("jwt_token", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge());
        assertEquals(200, res.getStatusCodeValue());
        assertEquals("Logout successful", res.getBody().get("message"));
    }

    // webRegister
    @Test
    void webRegister_passwordsNotMatch() {
        String result = authService.webRegister("Test User", "test@email.com", "a", "b", "on", redirectAttributes);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("khớp"));
        assertEquals("redirect:/register", result);
    }

    @Test
    void webRegister_agreeTermsNull() {
        String result = authService.webRegister("Test User", "test@email.com", "a", "a", null, redirectAttributes);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("điều khoản"));
        assertEquals("redirect:/register", result);
    }

    @Test
    void webRegister_emailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        String result = authService.webRegister("Test User", "test@email.com", "a", "a", "on", redirectAttributes);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Email"));
        assertEquals("redirect:/register", result);
    }

    @Test
    void webRegister_usernameExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(true, false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        String result = authService.webRegister("Test User", "test@email.com", "a", "a", "on", redirectAttributes);
        verify(redirectAttributes).addFlashAttribute(eq("success"), contains("Đăng ký thành công"));
        assertEquals("redirect:/", result);
    }

    @Test
    void webRegister_success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        String result = authService.webRegister("Test User", "test@email.com", "a", "a", "on", redirectAttributes);
        verify(redirectAttributes).addFlashAttribute(eq("success"), contains("Đăng ký thành công"));
        assertEquals("redirect:/", result);
    }

    @Test
    void webRegister_saveException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        doThrow(new RuntimeException("fail")).when(userRepository).save(any(AppUser.class));
        String result = authService.webRegister("Test User", "test@email.com", "a", "a", "on", redirectAttributes);
        verify(redirectAttributes).addFlashAttribute(eq("error"), contains("lỗi"));
        assertEquals("redirect:/register", result);
    }
} 