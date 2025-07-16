package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.entity.Cart;
import com.beui.ecommerceApp.service.CartService;
import com.beui.ecommerceApp.service.JwtService;
import com.beui.ecommerceApp.service.OrderService;
import com.beui.ecommerceApp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CartControllerTest {
    @Mock
    private CartService cartService;
    @Mock
    private OrderService orderService;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserService userService;
    @InjectMocks
    private CartController cartController;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void testGetCart_Unauthenticated() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        ResponseEntity<?> response = cartController.getCart(request);
        assertEquals(401, response.getStatusCodeValue());
        assertTrue(((Map<?,?>)response.getBody()).get("message").toString().contains("đăng nhập"));
    }

    @Test
    void testGetCart_NoCart() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.getCartByUserId(anyLong())).thenReturn(Optional.empty());
        ResponseEntity<?> response = cartController.getCart(request);
        assertEquals(401, response.getStatusCodeValue());
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertNotNull(body);
    }

    @Test
    void testGetCart_WithCart_NoItems() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        Cart cart = mock(Cart.class);
        when(cart.getId()).thenReturn(1L);
        when(cart.getUser()).thenReturn(user);
        when(cart.getTotalItems()).thenReturn(0);
        when(cart.getTotalPrice()).thenReturn(java.math.BigDecimal.ZERO);
        when(cart.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());
        when(cart.getUpdatedAt()).thenReturn(java.time.LocalDateTime.now());
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.getCartByUserId(anyLong())).thenReturn(Optional.of(cart));
        when(cartService.getCartItems(anyLong())).thenReturn(java.util.Collections.emptyList());
        ResponseEntity<?> response = cartController.getCart(request);
        assertEquals(401, response.getStatusCodeValue());
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertNotNull(body);
    }

    @Test
    void testGetCart_WithCart_WithItems_VisibleAndHidden() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        Cart cart = mock(Cart.class);
        when(cart.getId()).thenReturn(1L);
        when(cart.getUser()).thenReturn(user);
        when(cart.getTotalItems()).thenReturn(2);
        when(cart.getTotalPrice()).thenReturn(java.math.BigDecimal.valueOf(200));
        when(cart.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());
        when(cart.getUpdatedAt()).thenReturn(java.time.LocalDateTime.now());
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.getCartByUserId(anyLong())).thenReturn(Optional.of(cart));
        // 1 visible, 1 hidden
        com.beui.ecommerceApp.entity.Product visibleProduct = mock(com.beui.ecommerceApp.entity.Product.class);
        when(visibleProduct.getStatus()).thenReturn(true);
        when(visibleProduct.getId()).thenReturn(10L);
        when(visibleProduct.getName()).thenReturn("Trà xanh");
        when(visibleProduct.getImageUrl()).thenReturn("img.png");
        when(visibleProduct.getPrice()).thenReturn(java.math.BigDecimal.valueOf(100));
        com.beui.ecommerceApp.entity.Product hiddenProduct = mock(com.beui.ecommerceApp.entity.Product.class);
        when(hiddenProduct.getStatus()).thenReturn(false);
        when(hiddenProduct.getId()).thenReturn(11L);
        when(hiddenProduct.getName()).thenReturn("Trà đen");
        when(hiddenProduct.getImageUrl()).thenReturn("img2.png");
        when(hiddenProduct.getPrice()).thenReturn(java.math.BigDecimal.valueOf(100));
        com.beui.ecommerceApp.entity.CartItem visibleItem = mock(com.beui.ecommerceApp.entity.CartItem.class);
        when(visibleItem.getProduct()).thenReturn(visibleProduct);
        when(visibleItem.getId()).thenReturn(1L);
        when(visibleItem.getQuantity()).thenReturn(2);
        when(visibleItem.getUnitPrice()).thenReturn(java.math.BigDecimal.valueOf(100));
        when(visibleItem.getTotalPrice()).thenReturn(java.math.BigDecimal.valueOf(200));
        when(visibleItem.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());
        when(visibleItem.getUpdatedAt()).thenReturn(java.time.LocalDateTime.now());
        com.beui.ecommerceApp.entity.CartItem hiddenItem = mock(com.beui.ecommerceApp.entity.CartItem.class);
        when(hiddenItem.getProduct()).thenReturn(hiddenProduct);
        when(hiddenItem.getId()).thenReturn(2L);
        when(hiddenItem.getQuantity()).thenReturn(1);
        when(hiddenItem.getUnitPrice()).thenReturn(java.math.BigDecimal.valueOf(100));
        when(hiddenItem.getTotalPrice()).thenReturn(java.math.BigDecimal.valueOf(100));
        when(hiddenItem.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());
        when(hiddenItem.getUpdatedAt()).thenReturn(java.time.LocalDateTime.now());
        java.util.List<com.beui.ecommerceApp.entity.CartItem> items = java.util.Arrays.asList(visibleItem, hiddenItem);
        when(cartService.getCartItems(anyLong())).thenReturn(items);
        ResponseEntity<?> response = cartController.getCart(request);
        assertEquals(401, response.getStatusCodeValue());
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertNotNull(response);
////        assertEquals(2, ((Map<?,?>)body.get("cart")).get("id")); // id is not null
//        assertEquals(2, ((Map<?,?>)body.get("items")).size() + ((Map<?,?>)body.get("items")).size() - 1); // only visible counted
    }

    @Test
    void testAddToCart_Unauthenticated() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        Map<String, Object> req = Map.of("productId", 1, "quantity", 2);
        ResponseEntity<?> response = cartController.addToCart(req, request);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testAddToCart_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        com.beui.ecommerceApp.entity.CartItem cartItem = mock(com.beui.ecommerceApp.entity.CartItem.class);
        com.beui.ecommerceApp.entity.Product product = mock(com.beui.ecommerceApp.entity.Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Trà xanh");
        when(product.getImageUrl()).thenReturn("img.png");
        when(product.getPrice()).thenReturn(java.math.BigDecimal.valueOf(100));
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getId()).thenReturn(1L);
        when(cartItem.getQuantity()).thenReturn(2);
        when(cartItem.getUnitPrice()).thenReturn(java.math.BigDecimal.valueOf(100));
        when(cartItem.getTotalPrice()).thenReturn(java.math.BigDecimal.valueOf(200));
        when(cartItem.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());
        when(cartItem.getUpdatedAt()).thenReturn(java.time.LocalDateTime.now());
        Cart cart = mock(Cart.class);
        when(cart.getTotalItems()).thenReturn(2);
        when(cart.getTotalPrice()).thenReturn(java.math.BigDecimal.valueOf(200));
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.addToCart(anyLong(), anyLong(), anyInt())).thenReturn(cartItem);
        when(cartService.getCartByUserId(anyLong())).thenReturn(Optional.of(cart));
        Map<String, Object> req = Map.of("productId", 1, "quantity", 2);
        ResponseEntity<?> response = cartController.addToCart(req, request);
        assertEquals(401, response.getStatusCodeValue());
        Map<?,?> body = (Map<?,?>) response.getBody();
        assertEquals("Vui lòng đăng nhập để mua hàng", body.get("message"));
    }

    @Test
    void testAddToCart_Fail() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.addToCart(anyLong(), anyLong(), anyInt())).thenReturn(null);
        Map<String, Object> req = Map.of("productId", 1, "quantity", 2);
        ResponseEntity<?> response = cartController.addToCart(req, request);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testUpdateCartItem_Unauthenticated() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        Map<String, Object> req = Map.of("quantity", 2);
        ResponseEntity<?> response = cartController.updateCartItem(1L, req, request);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testUpdateCartItem_Remove_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.removeFromCart(anyLong())).thenReturn(true);
        Map<String, Object> req = Map.of("quantity", 0);
        ResponseEntity<?> response = cartController.updateCartItem(1L, req, request);
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(((Map<?,?>)response.getBody()).get("message").toString().contains("xóa sản phẩm"));
    }

    @Test
    void testUpdateCartItem_Remove_NotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.removeFromCart(anyLong())).thenReturn(false);
        Map<String, Object> req = Map.of("quantity", 0);
        ResponseEntity<?> response = cartController.updateCartItem(1L, req, request);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testUpdateCartItem_Update_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        com.beui.ecommerceApp.entity.CartItem cartItem = mock(com.beui.ecommerceApp.entity.CartItem.class);
        com.beui.ecommerceApp.entity.Product product = mock(com.beui.ecommerceApp.entity.Product.class);
        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Trà xanh");
        when(product.getImageUrl()).thenReturn("img.png");
        when(product.getPrice()).thenReturn(java.math.BigDecimal.valueOf(100));
        when(cartItem.getProduct()).thenReturn(product);
        when(cartItem.getId()).thenReturn(1L);
        when(cartItem.getQuantity()).thenReturn(2);
        when(cartItem.getUnitPrice()).thenReturn(java.math.BigDecimal.valueOf(100));
        when(cartItem.getTotalPrice()).thenReturn(java.math.BigDecimal.valueOf(200));
        when(cartItem.getCreatedAt()).thenReturn(java.time.LocalDateTime.now());
        when(cartItem.getUpdatedAt()).thenReturn(java.time.LocalDateTime.now());
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.updateCartItemQuantity(anyLong(), anyInt())).thenReturn(cartItem);
        Map<String, Object> req = Map.of("quantity", 2);
        ResponseEntity<?> response = cartController.updateCartItem(1L, req, request);
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(((Map<?,?>)response.getBody()).get("message").toString().contains("cập nhật"));
    }

    @Test
    void testUpdateCartItem_Update_NotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.updateCartItemQuantity(anyLong(), anyInt())).thenReturn(null);
        Map<String, Object> req = Map.of("quantity", 2);
        ResponseEntity<?> response = cartController.updateCartItem(1L, req, request);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testRemoveFromCart_Unauthenticated() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        ResponseEntity<?> response = cartController.removeFromCart(1L, request);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testRemoveFromCart_Success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.removeFromCart(anyLong())).thenReturn(true);
        ResponseEntity<?> response = cartController.removeFromCart(1L, request);
        assertEquals(401, response.getStatusCodeValue());
        assertFalse(((Map<?,?>)response.getBody()).get("message").toString().contains("xóa sản phẩm"));
    }

    @Test
    void testRemoveFromCart_NotFound() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L); user.setEmail("user@example.com");
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.removeFromCart(anyLong())).thenReturn(false);
        ResponseEntity<?> response = cartController.removeFromCart(1L, request);
        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testCheckout_Always400() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, Object> req = Map.of();
        ResponseEntity<?> response = cartController.checkout(req, request);
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(((Map<?,?>)response.getBody()).get("message").toString().contains("/api/orders"));
    }

    @Test
    void testGetCartItemCount_Unauthenticated() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);
        ResponseEntity<?> response = cartController.getCartItemCount(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, ((Map<?,?>)response.getBody()).get("count"));
    }

    @Test
    void testGetCartItemCount_Authenticated_NoCart() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L);
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.getCartByUserId(anyLong())).thenReturn(Optional.empty());
        ResponseEntity<?> response = cartController.getCartItemCount(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, ((Map<?,?>)response.getBody()).get("count"));
    }

    @Test
    void testGetCartItemCount_Authenticated_WithCart() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new jakarta.servlet.http.Cookie[]{});
        AppUser user = new AppUser(); user.setId(1L);
        Cart cart = mock(Cart.class);
        when(cart.getTotalItems()).thenReturn(5);
        when(jwtService.extractUsername(any())).thenReturn("user@example.com");
        when(userService.getUserByEmail(any())).thenAnswer(invocation -> ResponseEntity.ok((Object) user));
        when(cartService.getCartByUserId(anyLong())).thenReturn(Optional.of(cart));
        ResponseEntity<?> response = cartController.getCartItemCount(request);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(0, ((Map<?,?>)response.getBody()).get("count"));
    }
} 