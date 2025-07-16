package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.entity.Cart;
import com.beui.ecommerceApp.entity.CartItem;
import com.beui.ecommerceApp.entity.Product;
import com.beui.ecommerceApp.repository.AppUserRepository;
import com.beui.ecommerceApp.repository.CartItemRepository;
import com.beui.ecommerceApp.repository.CartRepository;
import com.beui.ecommerceApp.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CartServiceImplTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private AppUserRepository userRepository;
    @Spy
    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCartByUserId_found() {
        Cart cart = new Cart();
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        assertTrue(cartService.getCartByUserId(1L).isPresent());
    }

    @Test
    void getCartByUserId_notFound() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertFalse(cartService.getCartByUserId(1L).isPresent());
    }

    @Test
    void createCartForUser_success() {
        AppUser user = new AppUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Cart cart = new Cart(user);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        assertNotNull(cartService.createCartForUser(1L));
    }

    @Test
    void createCartForUser_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertNull(cartService.createCartForUser(1L));
    }

    @Test
    void addToCart_cartNull() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertNull(cartService.addToCart(1L, 2L, 1));
    }

    @Test
    void addToCart_productNotFound() {
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(2L)).thenReturn(Optional.empty());
        assertNull(cartService.addToCart(1L, 2L, 1));
    }

    @Test
    void addToCart_existingItem() {
        Cart cart = new Cart();
        cart.setId(1L);
        Product product = new Product();
        product.setId(2L);
        product.setPrice(BigDecimal.TEN);
        CartItem item = new CartItem(cart, product, 1, BigDecimal.TEN);
        item.setId(3L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 2L)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(item);
        doNothing().when(cartService).updateCartTotals(1L);
        CartItem result = cartService.addToCart(1L, 2L, 2);
        assertEquals(3, result.getQuantity());
    }

    @Test
    void addToCart_newItem() {
        Cart cart = new Cart();
        cart.setId(1L);
        Product product = new Product();
        product.setId(2L);
        product.setPrice(BigDecimal.TEN);
        CartItem newItem = new CartItem(cart, product, 2, BigDecimal.TEN);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product));
        when(cartItemRepository.findByCartIdAndProductId(1L, 2L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(newItem);
        doNothing().when(cartService).updateCartTotals(1L);
        CartItem result = cartService.addToCart(1L, 2L, 2);
        assertEquals(2, result.getQuantity());
    }

    @Test
    void updateCartItemQuantity_found() {
        Cart cart = new Cart();
        cart.setId(1L);
        CartItem item = new CartItem();
        item.setId(2L);
        item.setCart(cart);
        when(cartItemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(item);
        doNothing().when(cartService).updateCartTotals(1L);
        CartItem result = cartService.updateCartItemQuantity(2L, 5);
        assertEquals(5, result.getQuantity());
    }

    @Test
    void updateCartItemQuantity_notFound() {
        when(cartItemRepository.findById(2L)).thenReturn(Optional.empty());
        assertNull(cartService.updateCartItemQuantity(2L, 5));
    }

    @Test
    void removeFromCart_found() {
        Cart cart = new Cart();
        cart.setId(1L);
        CartItem item = new CartItem();
        item.setId(2L);
        item.setCart(cart);
        when(cartItemRepository.findById(2L)).thenReturn(Optional.of(item));
        doNothing().when(cartItemRepository).deleteById(2L);
        doNothing().when(cartService).updateCartTotals(1L);
        assertTrue(cartService.removeFromCart(2L));
    }

    @Test
    void removeFromCart_notFound() {
        when(cartItemRepository.findById(2L)).thenReturn(Optional.empty());
        assertFalse(cartService.removeFromCart(2L));
    }

    @Test
    void removeFromCart_exception() {
        when(cartItemRepository.findById(2L)).thenThrow(new RuntimeException("fail"));
        assertFalse(cartService.removeFromCart(2L));
    }

    @Test
    void getCartItems() {
        List<CartItem> items = Arrays.asList(new CartItem(), new CartItem());
        when(cartItemRepository.findByCartId(1L)).thenReturn(items);
        assertEquals(2, cartService.getCartItems(1L).size());
    }

    @Test
    void getCartItemsByUserId() {
        List<CartItem> items = Arrays.asList(new CartItem(), new CartItem());
        when(cartItemRepository.findByUserId(1L)).thenReturn(items);
        assertEquals(2, cartService.getCartItemsByUserId(1L).size());
    }

    @Test
    void calculateCartTotal() {
        CartItem item1 = mock(CartItem.class);
        CartItem item2 = mock(CartItem.class);
        when(item1.getTotalPrice()).thenReturn(BigDecimal.TEN);
        when(item2.getTotalPrice()).thenReturn(BigDecimal.ONE);
        when(cartItemRepository.findByCartId(1L)).thenReturn(Arrays.asList(item1, item2));
        assertEquals(BigDecimal.valueOf(11), cartService.calculateCartTotal(1L));
    }

    @Test
    void getCartItemCount() {
        when(cartItemRepository.countByCartId(1L)).thenReturn(5L);
        assertEquals(5L, cartService.getCartItemCount(1L));
    }

    @Test
    void updateCartTotals_found() {
        Cart cart = new Cart();
        cart.setId(1L);
        CartItem item1 = mock(CartItem.class);
        CartItem item2 = mock(CartItem.class);
        when(item1.getTotalPrice()).thenReturn(BigDecimal.TEN);
        when(item2.getTotalPrice()).thenReturn(BigDecimal.ONE);
        when(item1.getQuantity()).thenReturn(2);
        when(item2.getQuantity()).thenReturn(3);
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(Arrays.asList(item1, item2));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        cartService.updateCartTotals(1L);
        assertEquals(BigDecimal.valueOf(11), cart.getTotalPrice());
        assertEquals(5, cart.getTotalItems());
    }

    @Test
    void updateCartTotals_notFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());
        cartService.updateCartTotals(1L);
        // no exception
    }

    @Test
    void clearCart_found() {
        Cart cart = new Cart();
        cart.setId(1L);
        List<CartItem> items = Arrays.asList(new CartItem(), new CartItem());
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(items);
        doNothing().when(cartItemRepository).deleteAll(items);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        assertTrue(cartService.clearCart(1L));
        assertEquals(BigDecimal.ZERO, cart.getTotalPrice());
        assertEquals(0, cart.getTotalItems());
    }

    @Test
    void clearCart_notFound() {
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());
        assertFalse(cartService.clearCart(1L));
    }

    @Test
    void isCartEmpty_true() {
        when(cartItemRepository.countByCartId(1L)).thenReturn(0L);
        assertTrue(cartService.isCartEmpty(1L));
    }

    @Test
    void isCartEmpty_false() {
        when(cartItemRepository.countByCartId(1L)).thenReturn(2L);
        assertFalse(cartService.isCartEmpty(1L));
    }

    @Test
    void getCartItemById_found() {
        CartItem item = new CartItem();
        when(cartItemRepository.findById(2L)).thenReturn(Optional.of(item));
        assertEquals(item, cartService.getCartItemById(2L));
    }

    @Test
    void getCartItemById_notFound() {
        when(cartItemRepository.findById(2L)).thenReturn(Optional.empty());
        assertNull(cartService.getCartItemById(2L));
    }

    @Test
    void removeMultipleCartItems_nullOrEmpty() {
        cartService.removeMultipleCartItems(null);
        cartService.removeMultipleCartItems(Collections.emptyList());
        // no exception
    }

    @Test
    void removeMultipleCartItems_success() {
        Cart cart = new Cart();
        cart.setId(1L);
        CartItem item = new CartItem();
        item.setId(2L);
        item.setCart(cart);
        List<Long> ids = Arrays.asList(2L);
        when(cartItemRepository.findById(2L)).thenReturn(Optional.of(item));
        doNothing().when(cartItemRepository).deleteAllById(ids);
        doNothing().when(cartService).updateCartTotals(1L);
        cartService.removeMultipleCartItems(ids);
        // no exception
    }

    @Test
    void removeMultipleCartItems_exception() {
        List<Long> ids = Arrays.asList(2L);
        when(cartItemRepository.findById(2L)).thenThrow(new RuntimeException("fail"));
        assertThrows(RuntimeException.class, () -> cartService.removeMultipleCartItems(ids));
    }

    @Test
    void clearUserCart() {
        CartItem item = new CartItem();
        item.setId(2L);
        List<CartItem> items = Arrays.asList(item);
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartItemRepository.findByUserId(1L)).thenReturn(items);
        doNothing().when(cartItemRepository).deleteById(2L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        cartService.clearUserCart(1L);
        assertEquals(BigDecimal.ZERO, cart.getTotalPrice());
        assertEquals(0, cart.getTotalItems());
    }
} 