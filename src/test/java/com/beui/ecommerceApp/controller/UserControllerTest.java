package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {
    @Mock private UserService userService;
    @InjectMocks private UserController userController;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void testGetCurrentUser() {
        Authentication auth = mock(Authentication.class);
        when(userService.getCurrentUser(auth)).thenAnswer(invocation -> ResponseEntity.ok().body(new AppUser()));
        var response = userController.getCurrentUser(auth);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetUserById() {
        when(userService.getUserById(1L)).thenAnswer(invocation -> ResponseEntity.ok().body(new AppUser()));
        var response = userController.getUserById(1L);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetAllUsers() {
        when(userService.getAllUsers()).thenAnswer(invocation -> ResponseEntity.ok().body(java.util.Collections.emptyList()));
        var response = userController.getAllUsers();
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testGetUserByEmail() {
        when(userService.getUserByEmail("abc")).thenAnswer(invocation -> ResponseEntity.ok().body(new AppUser()));
        var response = userController.getUserByEmail("abc");
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testCreateUser() {
        AppUser user = new AppUser();
        when(userService.createUser(user)).thenAnswer(invocation -> ResponseEntity.ok().body(user));
        var response = userController.createUser(user);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testUpdateUser() {
        AppUser user = new AppUser();
        when(userService.updateUser(1L, user)).thenAnswer(invocation -> ResponseEntity.ok().body(user));
        var response = userController.updateUser(1L, user);
        assertEquals(200, response.getStatusCodeValue());
    }

    @Test
    void testDeleteUser() {
        when(userService.deleteUser(1L)).thenAnswer(invocation -> ResponseEntity.ok().build());
        var response = userController.deleteUser(1L);
        assertEquals(200, response.getStatusCodeValue());
    }
} 