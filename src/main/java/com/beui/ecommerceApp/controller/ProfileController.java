package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.dto.UserInfo;
import com.beui.ecommerceApp.service.FileUploadService;
import com.beui.ecommerceApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Debug logging for authentication
        System.out.println("🔍 Debug ProfileController.showProfile - Authentication:");
        System.out.println("  - Authentication object: " + (authentication != null ? "EXISTS" : "NULL"));
        System.out.println("  - Is authenticated: " + (authentication != null ? authentication.isAuthenticated() : "N/A"));
        System.out.println("  - Principal: " + (authentication != null ? authentication.getPrincipal() : "N/A"));
        System.out.println("  - Authorities: " + (authentication != null ? authentication.getAuthorities() : "N/A"));
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            System.out.println("❌ Redirecting to home - User not authenticated");
            return "redirect:/";
        }
        
        String username = authentication.getName();
        System.out.println("✅ User authenticated: " + username);
        
        try {
            UserInfo userInfo = userService.getUserInfoByUsername(username);
            model.addAttribute("user", userInfo);
            // Add authentication info for header
            model.addAttribute("isAuthenticated", true);
            model.addAttribute("currentUser", userInfo);
            // Debug logging
            System.out.println("🔍 Debug ProfileController.showProfile:");
            System.out.println("  - Username: " + username);
            System.out.println("  - UserInfo email: " + (userInfo != null ? userInfo.getEmail() : "NULL"));
            System.out.println("  - UserInfo username: " + (userInfo != null ? userInfo.getUsername() : "NULL"));
            System.out.println("  - UserInfo fullName: " + (userInfo != null ? userInfo.getFullName() : "NULL"));
            return "profile";
        } catch (Exception e) {
            System.out.println("❌ Error loading user info: " + e.getMessage());
            model.addAttribute("error", "Không thể tải thông tin người dùng");
            return "redirect:/";
        }
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute UserInfo userInfo, 
                              RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserProfile(userInfo);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cập nhật thất bại: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không khớp!");
                return "redirect:/profile";
            }
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            userService.changePassword(username, currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đổi mật khẩu thất bại: " + e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("avatar") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Map<String, Object> response = new HashMap<>();
        
        try {
            String url = fileUploadService.uploadUserAvatar(file, username);
            userService.updateUserAvatarUrl(username, url);
            response.put("success", true);
            response.put("message", "Cập nhật ảnh đại diện thành công!");
            response.put("avatarUrl", url);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi upload ảnh: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 