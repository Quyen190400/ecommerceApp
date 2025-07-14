package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Get current user information")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        return userService.getCurrentUser(auth);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Get user by ID (requires ADMIN role)")
    public ResponseEntity<?> getUserById(@PathVariable("id") Long id) {
        return userService.getUserById(id);
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Get all users (requires ADMIN role)")
    public ResponseEntity<?> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable("email") String email) {
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
        return userService.updateUser(id, userDetails);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(description = "Delete user by ID (requires ADMIN role)")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        return userService.deleteUser(id);
    }
} 