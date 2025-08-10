package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.dto.UserInfo;
import com.beui.ecommerceApp.service.UserService;
import com.beui.ecommerceApp.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileControllerTest {

    @Mock
    private UserService userService;
    
    @Mock
    private FileUploadService fileUploadService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void showProfile_Success() {
        // Arrange
        String username = "test@example.com";
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setFullName("Test User");
        userInfo.setEmail(username);

        when(authentication.getName()).thenReturn(username);
        when(userService.getUserInfoByUsername(username)).thenReturn(userInfo);

        // Act
        String result = profileController.showProfile(model);

        // Assert
        assertEquals("profile", result);
        verify(model).addAttribute("user", userInfo);
    }

    @Test
    void showProfile_Exception() {
        // Arrange
        String username = "test@example.com";
        when(authentication.getName()).thenReturn(username);
        when(userService.getUserInfoByUsername(username)).thenThrow(new RuntimeException("User not found"));

        // Act
        String result = profileController.showProfile(model);

        // Assert
        assertEquals("redirect:/", result);
        verify(model).addAttribute("error", "Không thể tải thông tin người dùng");
    }

    @Test
    void updateProfile_Success() {
        // Arrange
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        userInfo.setFullName("Updated Name");
        userInfo.setPhone("0987654321");

        // Act
        String result = profileController.updateProfile(userInfo, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", result);
        verify(userService).updateUserProfile(userInfo);
        verify(redirectAttributes).addFlashAttribute("success", "Cập nhật thông tin thành công!");
    }

    @Test
    void updateProfile_Exception() {
        // Arrange
        UserInfo userInfo = new UserInfo();
        userInfo.setId(1L);
        doThrow(new RuntimeException("Update failed")).when(userService).updateUserProfile(userInfo);

        // Act
        String result = profileController.updateProfile(userInfo, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", result);
        verify(redirectAttributes).addFlashAttribute("error", "Cập nhật thất bại: Update failed");
    }

    @Test
    void changePassword_Success() {
        // Arrange
        String username = "test@example.com";
        String currentPassword = "oldpass";
        String newPassword = "newpass";
        String confirmPassword = "newpass";

        when(authentication.getName()).thenReturn(username);
        when(userService.changePassword(username, currentPassword, newPassword)).thenReturn(true);

        // Act
        String result = profileController.changePassword(currentPassword, newPassword, confirmPassword, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", result);
        verify(userService).changePassword(username, currentPassword, newPassword);
        verify(redirectAttributes).addFlashAttribute("success", "Đổi mật khẩu thành công!");
    }

    @Test
    void changePassword_PasswordMismatch() {
        // Arrange
        String currentPassword = "oldpass";
        String newPassword = "newpass";
        String confirmPassword = "differentpass";

        // Act
        String result = profileController.changePassword(currentPassword, newPassword, confirmPassword, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", result);
        verify(redirectAttributes).addFlashAttribute("error", "Mật khẩu mới không khớp!");
        verify(userService, never()).changePassword(any(String.class), any(String.class), any(String.class));
    }

    @Test
    void changePassword_Exception() {
        // Arrange
        String username = "test@example.com";
        String currentPassword = "oldpass";
        String newPassword = "newpass";
        String confirmPassword = "newpass";

        when(authentication.getName()).thenReturn(username);
        when(userService.changePassword(username, currentPassword, newPassword)).thenReturn(false);

        // Act
        String result = profileController.changePassword(currentPassword, newPassword, confirmPassword, redirectAttributes);

        // Assert
        assertEquals("redirect:/profile", result);
        verify(redirectAttributes).addFlashAttribute("error", "Đổi mật khẩu thất bại: false");
    }

    @Test
    void uploadAvatar_Success() {
        // Arrange
        String username = "test@example.com";
        String avatarUrl = "https://res.cloudinary.com/example/image/upload/v123/avatar_test.jpg";
        MockMultipartFile file = new MockMultipartFile(
            "avatar", 
            "test.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );
        
        when(authentication.getName()).thenReturn(username);
        when(fileUploadService.uploadUserAvatar(file, username)).thenReturn(avatarUrl);
        
        // Act
        String result = profileController.uploadAvatar(file, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/profile", result);
        verify(fileUploadService).uploadUserAvatar(file, username);
        verify(userService).updateUserAvatarUrl(username, avatarUrl);
        verify(redirectAttributes).addFlashAttribute("success", "Cập nhật ảnh đại diện thành công!");
    }

    @Test
    void uploadAvatar_Exception() {
        // Arrange
        String username = "test@example.com";
        MockMultipartFile file = new MockMultipartFile(
            "avatar", 
            "test.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );
        
        when(authentication.getName()).thenReturn(username);
        when(fileUploadService.uploadUserAvatar(file, username)).thenThrow(new RuntimeException("Upload failed"));
        
        // Act
        String result = profileController.uploadAvatar(file, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/profile", result);
        verify(redirectAttributes).addFlashAttribute("error", "Lỗi upload ảnh: Upload failed");
    }
} 