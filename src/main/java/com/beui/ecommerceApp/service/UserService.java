package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.entity.AppUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface UserService {
    ResponseEntity<?> getCurrentUser(Authentication auth);
    ResponseEntity<?> getUserById(Long id);
    ResponseEntity<?> getAllUsers();
    ResponseEntity<?> getUserByEmail(String email);
    ResponseEntity<?> createUser(AppUser user);
    ResponseEntity<?> updateUser(Long id, AppUser userDetails);
    ResponseEntity<?> deleteUser(Long id);
} 