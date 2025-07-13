package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.entity.Cart;
import com.beui.ecommerceApp.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CartService {
    Optional<Cart> getCartByUserId(Long userId);
    Cart createCartForUser(Long userId);
    CartItem addToCart(Long userId, Long productId, Integer quantity);
    CartItem updateCartItemQuantity(Long cartItemId, Integer quantity);
    boolean removeFromCart(Long cartItemId);
    List<CartItem> getCartItems(Long cartId);
    List<CartItem> getCartItemsByUserId(Long userId);
    BigDecimal calculateCartTotal(Long cartId);
    Long getCartItemCount(Long cartId);
    void updateCartTotals(Long cartId);
    boolean clearCart(Long cartId);
    boolean isCartEmpty(Long cartId);
    CartItem getCartItemById(Long cartItemId);
    void removeMultipleCartItems(List<Long> cartItemIds);
    void clearUserCart(Long userId);
} 