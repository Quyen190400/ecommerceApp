package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.dto.CartItemResponse;
import com.beui.ecommerceApp.dto.CartResponse;
import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.entity.Cart;
import com.beui.ecommerceApp.entity.CartItem;
import com.beui.ecommerceApp.service.CartService;
import com.beui.ecommerceApp.service.JwtService;
import com.beui.ecommerceApp.service.OrderService;
import com.beui.ecommerceApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Cart", description = "APIs for managing the user's shopping cart: view, add, update, remove items, and checkout.")
@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Helper method to get current user from JWT
     */
    private Optional<AppUser> getCurrentUser(HttpServletRequest request) {
        try {
            // Get JWT token from cookie
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
                return Optional.empty();
            }
            
            // Validate JWT token
            String email = jwtService.extractUsername(token);
            if (email == null) {
                return Optional.empty();
            }
            
            // Get user details
            ResponseEntity<?> userResponse = userService.getUserByEmail(email);
            AppUser user = null;
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() instanceof AppUser) {
                user = (AppUser) userResponse.getBody();
            } else {
                // Nếu body là Optional hoặc object khác, trả về empty
                return Optional.empty();
            }
            Optional<AppUser> userOpt = Optional.ofNullable(user);
            return userOpt;
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Convert CartItem to CartItemResponse DTO
     */
    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        CartItemResponse response = new CartItemResponse();
        response.setId(cartItem.getId());
        response.setProductId(cartItem.getProduct().getId());
        response.setProductName(cartItem.getProduct().getName());
        response.setProductImage(cartItem.getProduct().getImageUrl());
        response.setProductPrice(cartItem.getProduct().getPrice());
        response.setQuantity(cartItem.getQuantity());
        response.setUnitPrice(cartItem.getUnitPrice());
        response.setTotalPrice(cartItem.getTotalPrice());
        response.setCreatedAt(cartItem.getCreatedAt());
        response.setUpdatedAt(cartItem.getUpdatedAt());
        return response;
    }
    
    /**
     * Convert Cart to CartResponse DTO
     */
    private CartResponse convertToCartResponse(Cart cart, List<CartItem> cartItems) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setUserId(cart.getUser().getId());
        response.setUserEmail(cart.getUser().getEmail());
        response.setTotalPrice(cart.getTotalPrice());
        response.setTotalItems(cart.getTotalItems());
        response.setCreatedAt(cart.getCreatedAt());
        response.setUpdatedAt(cart.getUpdatedAt());
        
        // Convert cart items to DTOs
        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);
        
        return response;
    }
    
    @Operation(description = "Get the current user's cart with all visible items")
    // Lấy giỏ hàng của user hiện tại
    @GetMapping
    public ResponseEntity<?> getCart(HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập để xem giỏ hàng"));
        }
        
        AppUser user = userOpt.get();
        Optional<Cart> cartOpt = cartService.getCartByUserId(user.getId());
        
        if (!cartOpt.isPresent()) {
            return ResponseEntity.ok(Map.of(
                "cart", null,
                "items", List.of(),
                "totalItems", 0,
                "totalPrice", BigDecimal.ZERO
            ));
        }
        
        Cart cart = cartOpt.get();
        List<CartItem> items = cartService.getCartItems(cart.getId());
        
        // Filter out hidden products
        List<CartItem> visibleItems = items.stream()
                .filter(item -> item.getProduct().getStatus() != null && item.getProduct().getStatus())
                .collect(Collectors.toList());
        
        // Cập nhật totals từ items thực tế (chỉ visible items)
        int totalItems = visibleItems.stream().mapToInt(CartItem::getQuantity).sum();
        BigDecimal totalPrice = visibleItems.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Cập nhật cart totals trong database
        cartService.updateCartTotals(cart.getId());
        
        // Convert to DTOs (chỉ visible items)
        CartResponse cartResponse = convertToCartResponse(cart, visibleItems);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cart", cartResponse);
        response.put("items", cartResponse.getItems());
        response.put("totalItems", totalItems);
        response.put("totalPrice", totalPrice);
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(description = "Add a product to the current user's cart")
    // Thêm sản phẩm vào giỏ hàng
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Optional<AppUser> userOpt = getCurrentUser(httpRequest);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập để mua hàng"));
        }
        
        AppUser user = userOpt.get();
        Long productId = Long.valueOf(request.get("productId").toString());
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        
        CartItem cartItem = cartService.addToCart(user.getId(), productId, quantity);
        
        if (cartItem == null) {
            return ResponseEntity.status(400).body(Map.of("message", "Không thể thêm sản phẩm vào giỏ hàng"));
        }
        
        // Lấy thông tin giỏ hàng cập nhật
        Optional<Cart> cartOpt = cartService.getCartByUserId(user.getId());
        Cart cart = cartOpt.get();
        
        // Convert to DTO
        CartItemResponse cartItemResponse = convertToCartItemResponse(cartItem);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đã thêm vào giỏ hàng");
        response.put("cartItem", cartItemResponse);
        response.put("totalItems", cart.getTotalItems());
        response.put("totalPrice", cart.getTotalPrice());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(description = "Update the quantity of a cart item by item ID")
    // Cập nhật số lượng sản phẩm
    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateCartItem(@PathVariable("itemId") Long itemId, @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        Optional<AppUser> userOpt = getCurrentUser(httpRequest);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        
        Integer quantity = Integer.valueOf(request.get("quantity").toString());
        
        if (quantity <= 0) {
            // Nếu số lượng <= 0, xóa item
            boolean removed = cartService.removeFromCart(itemId);
            if (removed) {
                return ResponseEntity.ok(Map.of("message", "Đã xóa sản phẩm khỏi giỏ hàng"));
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy sản phẩm"));
            }
        }
        
        CartItem updatedItem = cartService.updateCartItemQuantity(itemId, quantity);
        
        if (updatedItem == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy sản phẩm"));
        }
        
        // Convert to DTO
        CartItemResponse cartItemResponse = convertToCartItemResponse(updatedItem);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đã cập nhật số lượng");
        response.put("cartItem", cartItemResponse);
        response.put("totalPrice", cartItemResponse.getTotalPrice());
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(description = "Remove a product from the cart by item ID")
    // Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable("itemId") Long itemId, HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        
        boolean removed = cartService.removeFromCart(itemId);
        
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "Đã xóa sản phẩm khỏi giỏ hàng"));
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy sản phẩm"));
        }
    }
    
    @Operation(description = "Checkout the cart (redirects to /api/orders for full order creation)")
    // Đặt hàng từ giỏ hàng - Redirect to new order API
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        return ResponseEntity.status(400).body(Map.of("message", "Vui lòng sử dụng API /api/orders để đặt hàng với thông tin đầy đủ"));
    }
    
    @Operation(description = "Get the total number of items in the current user's cart")
    // Lấy số lượng item trong giỏ hàng
    @GetMapping("/count")
    public ResponseEntity<?> getCartItemCount(HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
        
        AppUser user = userOpt.get();
        Optional<Cart> cartOpt = cartService.getCartByUserId(user.getId());
        
        if (!cartOpt.isPresent()) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
        
        Cart cart = cartOpt.get();
        return ResponseEntity.ok(Map.of("count", cart.getTotalItems()));
    }
} 