package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.dto.LoginRequest;
import com.beui.ecommerceApp.dto.LoginResponse;
import com.beui.ecommerceApp.dto.RegisterRequest;
import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(loginRequest.getEmail());
                AppUser user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
                Cookie jwtCookie = new Cookie("jwt_token", token);
                jwtCookie.setHttpOnly(true);
                jwtCookie.setSecure(false);
                jwtCookie.setPath("/");
                jwtCookie.setMaxAge(24 * 60 * 60);
                response.addCookie(jwtCookie);
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setToken(token);
                loginResponse.setMessage("Login successful");
                loginResponse.setUsername(user.getUsername());
                loginResponse.setRole(user.getRole());
                return ResponseEntity.ok(loginResponse);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                new LoginResponse(null, "Invalid email or password")
            );
        }
        return ResponseEntity.badRequest().body(
            new LoginResponse(null, "Authentication failed")
        );
    }

    @Override
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Cookie[] cookies = request.getCookies();
            String token = null;
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt_token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
            if (token == null) {
                response.put("authenticated", false);
                response.put("message", "No JWT token found");
                return ResponseEntity.status(401).body(response);
            }
            String email = jwtService.extractUsername(token);
            if (email == null) {
                response.put("authenticated", false);
                response.put("message", "Invalid JWT token");
                return ResponseEntity.status(401).body(response);
            }
            Optional<AppUser> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                response.put("authenticated", false);
                response.put("message", "User not found");
                return ResponseEntity.status(401).body(response);
            }
            AppUser user = userOpt.get();
            response.put("authenticated", true);
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("authenticated", false);
            response.put("message", "Error processing request: " + e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    @Override
    public ResponseEntity<Map<String, Object>> register(RegisterRequest registerRequest) {
        Map<String, Object> response = new HashMap<>();
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            response.put("success", false);
            response.put("message", "Passwords do not match");
            return ResponseEntity.badRequest().body(response);
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            response.put("success", false);
            response.put("message", "Email already exists");
            return ResponseEntity.badRequest().body(response);
        }
        String email = registerRequest.getEmail();
        String username = email.split("@")[0];
        String finalUsername = username;
        int counter = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = username + counter;
            counter++;
        }
        AppUser user = new AppUser();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setUsername(finalUsername);
        user.setRole("CUSTOMER");
        userRepository.save(user);
        response.put("success", true);
        response.put("message", "User registered successfully");
        response.put("username", finalUsername);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        // Clear JWT cookie
        Cookie jwtCookie = new Cookie("jwt_token", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        
        // Clear JSESSIONID cookie
        Cookie sessionCookie = new Cookie("JSESSIONID", "");
        sessionCookie.setHttpOnly(true);
        sessionCookie.setSecure(false);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        response.addCookie(sessionCookie);
        
        // Clear SecurityContext
        SecurityContextHolder.clearContext();
        
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Logout successful");
        return ResponseEntity.ok(responseMap);
    }

    @Override
    public String webRegister(String fullName, String email, String password, String confirmPassword, String agreeTerms, RedirectAttributes redirectAttributes) {
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
            return "redirect:/register";
        }
        if (agreeTerms == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đồng ý với điều khoản");
            return "redirect:/register";
        }
        if (userRepository.existsByEmail(email)) {
            redirectAttributes.addFlashAttribute("error", "Email đã tồn tại");
            return "redirect:/register";
        }
        String username = email.split("@")[0];
        String finalUsername = username;
        int counter = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = username + counter;
            counter++;
        }
        AppUser user = new AppUser();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(finalUsername);
        user.setRole("CUSTOMER");
        try {
            userRepository.save(user);
            System.out.println("✅ User registered successfully: " + user.getEmail());
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Chào mừng bạn đến với Beui Tea Shop.");
            System.out.println("🔄 Redirecting to home page...");
            return "redirect:/";
        } catch (Exception e) {
            System.out.println("❌ Error registering user: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đăng ký");
            return "redirect:/register";
        }
    }
} 