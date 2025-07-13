package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.entity.Cart;
import com.beui.ecommerceApp.entity.CartItem;
import com.beui.ecommerceApp.entity.Product;
import com.beui.ecommerceApp.repository.ProductRepository;
import com.beui.ecommerceApp.repository.CartRepository;
import com.beui.ecommerceApp.repository.CartItemRepository;
import com.beui.ecommerceApp.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AppUserRepository userRepository;

    @Override
    public Optional<Cart> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Cart createCartForUser(Long userId) {
        Optional<AppUser> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            Cart cart = new Cart(userOpt.get());
            return cartRepository.save(cart);
        }
        return null;
    }

    @Override
    @Transactional
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = getCartByUserId(userId).orElseGet(() -> createCartForUser(userId));
        if (cart == null) {
            return null;
        }
        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            return null;
        }
        Product product = productOpt.get();
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            CartItem savedItem = cartItemRepository.save(item);
            updateCartTotals(cart.getId());
            return savedItem;
        } else {
            CartItem newItem = new CartItem(cart, product, quantity, product.getPrice());
            CartItem savedItem = cartItemRepository.save(newItem);
            updateCartTotals(cart.getId());
            return savedItem;
        }
    }

    @Override
    @Transactional
    public CartItem updateCartItemQuantity(Long cartItemId, Integer quantity) {
        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        if (itemOpt.isPresent()) {
            CartItem item = itemOpt.get();
            item.setQuantity(quantity);
            CartItem savedItem = cartItemRepository.save(item);
            updateCartTotals(item.getCart().getId());
            return savedItem;
        }
        return null;
    }

    @Override
    @Transactional
    public boolean removeFromCart(Long cartItemId) {
        try {
            Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
            if (itemOpt.isPresent()) {
                CartItem item = itemOpt.get();
                Long cartId = item.getCart().getId();
                cartItemRepository.deleteById(cartItemId);
                updateCartTotals(cartId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.out.println("Error removing cart item " + cartItemId + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<CartItem> getCartItems(Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    @Override
    public List<CartItem> getCartItemsByUserId(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Override
    public BigDecimal calculateCartTotal(Long cartId) {
        List<CartItem> items = getCartItems(cartId);
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Long getCartItemCount(Long cartId) {
        return cartItemRepository.countByCartId(cartId);
    }

    @Override
    @Transactional
    public void updateCartTotals(Long cartId) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            List<CartItem> items = cartItemRepository.findByCartId(cartId);
            BigDecimal totalPrice = items.stream()
                    .map(CartItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int totalItems = items.stream()
                    .mapToInt(CartItem::getQuantity)
                    .sum();
            cart.setTotalPrice(totalPrice);
            cart.setTotalItems(totalItems);
            cartRepository.save(cart);
        }
    }

    @Override
    @Transactional
    public boolean clearCart(Long cartId) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        if (cartOpt.isPresent()) {
            List<CartItem> items = getCartItems(cartId);
            cartItemRepository.deleteAll(items);
            Cart cart = cartOpt.get();
            cart.setTotalPrice(BigDecimal.ZERO);
            cart.setTotalItems(0);
            cartRepository.save(cart);
            return true;
        }
        return false;
    }

    @Override
    public boolean isCartEmpty(Long cartId) {
        return getCartItemCount(cartId) == 0;
    }

    @Override
    public CartItem getCartItemById(Long cartItemId) {
        Optional<CartItem> itemOpt = cartItemRepository.findById(cartItemId);
        return itemOpt.orElse(null);
    }

    @Override
    @Transactional
    public void removeMultipleCartItems(List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return;
        }
        try {
            Long cartId = null;
            for (Long cartItemId : cartItemIds) {
                Optional<CartItem> item = cartItemRepository.findById(cartItemId);
                if (item.isPresent() && cartId == null) {
                    cartId = item.get().getCart().getId();
                }
            }
            cartItemRepository.deleteAllById(cartItemIds);
            if (cartId != null) {
                updateCartTotals(cartId);
            }
            System.out.println("Successfully removed " + cartItemIds.size() + " cart items");
        } catch (Exception e) {
            System.out.println("Error removing multiple cart items: " + e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void clearUserCart(Long userId) {
        List<CartItem> userCartItems = getCartItemsByUserId(userId);
        for (CartItem item : userCartItems) {
            try {
                cartItemRepository.deleteById(item.getId());
            } catch (Exception e) {
                System.out.println("Warning: Could not remove cart item " + item.getId() + ": " + e.getMessage());
            }
        }
        Optional<Cart> cartOpt = getCartByUserId(userId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cart.setTotalPrice(BigDecimal.ZERO);
            cart.setTotalItems(0);
            cartRepository.save(cart);
        }
    }
} 