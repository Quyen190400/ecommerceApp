package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.dto.OrderRequest;
import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.entity.CustomerOrder;
import com.beui.ecommerceApp.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<CustomerOrder> getAllOrders();
    Optional<CustomerOrder> getOrderById(Long id);
    List<CustomerOrder> getOrdersByUserId(Long userId);
    List<CustomerOrder> getOrdersByStatus(String status);
    CustomerOrder createOrder(CustomerOrder order);
    CustomerOrder updateOrderStatus(Long orderId, String status);
    CustomerOrder cancelOrder(Long orderId);
    CustomerOrder confirmPayment(Long orderId);
    OrderItem addItemToOrder(Long orderId, Long productId, Integer quantity);
    List<OrderItem> getOrderItems(Long orderId);
    BigDecimal calculateOrderTotal(Long orderId);
    boolean deleteOrder(Long id);
    boolean canCancelOrder(Long orderId);
    boolean canConfirmPayment(Long orderId);
    CustomerOrder createBuyNowOrder(AppUser user, Long productId, Integer quantity, OrderRequest orderRequest);
    org.springframework.http.ResponseEntity<?> handleBuyNow(AppUser user, java.util.Map<String, Object> payload);
    List<CustomerOrder> filterOrdersByUser(Long userId, String status, String orderId, String startDate, String endDate, String search);
    Page<CustomerOrder> filterOrdersForAdmin(String status, String orderId, String startDate, String endDate, String search, int page, int size);
    Page<CustomerOrder> getAllOrders(int page, int size);
} 