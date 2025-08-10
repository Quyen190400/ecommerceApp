package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "User", description = "APIs for managing users: view, create, update, and delete user accounts.")
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    // Helper kiểm tra active
    private boolean isUserInactive(AppUser user) {
        return user.getActive() == null || !user.getActive();
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Get current user information")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        // Lấy email từ Authentication
        String email = auth.getName();
        // Lấy user từ repository
        AppUser user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy người dùng"));
        }
        if (isUserInactive(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên."));
        }
        // Trả về thông tin user (có thể dùng lại userService.getCurrentUser(auth) nếu muốn trả về DTO)
        return userService.getCurrentUser(auth);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Get user by ID (requires ADMIN role)")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id) {
        AppUser user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy người dùng"));
        }
        if (isUserInactive(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên."));
        }
        return userService.getUserById(id);
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
//    @PreAuthorize("hasRole('ADMIN')")
    @Operation(description = "Get all users (requires ADMIN role)")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return userService.getAllUsers(page, size, keyword);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable("email") String email) {
        AppUser user = userService.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy người dùng"));
        }
        if (isUserInactive(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên."));
        }
        return userService.getUserByEmail(email);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody AppUser user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Update user information")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody AppUser userDetails) {
        AppUser user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy người dùng"));
        }
        if (isUserInactive(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên."));
        }
        return userService.updateUser(id, userDetails);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Delete user by ID (requires ADMIN role)")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        AppUser user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy người dùng"));
        }
        if (isUserInactive(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Tài khoản của bạn đã bị vô hiệu hóa. Vui lòng liên hệ quản trị viên."));
        }
        return userService.deleteUser(id);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        boolean ok = userService.forgotPassword(email);
        if (ok) return ResponseEntity.ok().body(Map.of("message", "Mật khẩu mới đã được gửi đến email của bạn."));
        return ResponseEntity.badRequest().body(Map.of("message", "Email không tồn tại hoặc tài khoản bị khóa."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam Long userId, @RequestParam String oldPassword, @RequestParam String newPassword) {
        boolean ok = userService.changePassword(userId, oldPassword, newPassword);
        if (ok) return ResponseEntity.ok().body(Map.of("message", "Đổi mật khẩu thành công."));
        return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu hiện tại không đúng."));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/set-active")
    public ResponseEntity<?> setActive(@RequestParam("userId") Long userId, @RequestParam("active") boolean active) {
        userService.setActive(userId, active);
        return ResponseEntity.ok().body(Map.of("message", active ? "Đã kích hoạt tài khoản." : "Đã ngừng kích hoạt tài khoản."));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("userId") Long userId) {
        userService.resetPassword(userId, "TeaShop@123");
        // Gửi mail cho user
        userService.findById(userId).ifPresent(user -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Mật khẩu đã được reset");
            message.setText("Mật khẩu mới của bạn là: TeaShop@123");
            userService.sendMail(message);
        });
        return ResponseEntity.ok().body(Map.of("message", "Đã reset mật khẩu và gửi email cho người dùng."));
    }
} 