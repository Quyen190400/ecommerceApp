package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.entity.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import java.util.Optional;
import org.springframework.mail.SimpleMailMessage;

public interface UserService {
    ResponseEntity<?> getCurrentUser(Authentication auth);
    ResponseEntity<?> getUserById(Long id);
    ResponseEntity<?> getAllUsers();
    ResponseEntity<?> getUserByEmail(String email);
    ResponseEntity<?> createUser(AppUser user);
    ResponseEntity<?> updateUser(Long id, AppUser userDetails);
    ResponseEntity<?> deleteUser(Long id);
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    void setActive(Long userId, boolean active);
    void resetPassword(Long userId, String newPassword);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    boolean forgotPassword(String email);
    void sendMail(SimpleMailMessage message);
    Optional<AppUser> findById(Long id);
} 