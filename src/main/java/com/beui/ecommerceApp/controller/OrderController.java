package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.entity.CartItem;
import com.beui.ecommerceApp.entity.CustomerOrder;
import com.beui.ecommerceApp.entity.OrderItem;
import com.beui.ecommerceApp.service.CartService;
import com.beui.ecommerceApp.service.JwtService;
import com.beui.ecommerceApp.service.OrderService;
import com.beui.ecommerceApp.service.UserService;
import com.beui.ecommerceApp.dto.OrderRequest;
import com.beui.ecommerceApp.dto.OrderResponse;
import com.beui.ecommerceApp.dto.OrderItemResponse;
import com.beui.ecommerceApp.dto.UserInfo;
import com.beui.ecommerceApp.dto.ProductInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Order", description = "APIs for managing orders: place, view, update, cancel, and get order statistics.")
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserService userService;
    
    // Helper method to remove cart items in separate transaction
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void removeCartItemsInSeparateTransaction(List<Long> cartItemIds) {
        try {
            if (cartItemIds == null || cartItemIds.isEmpty()) {
                System.out.println("No cart items to remove");
                return;
            }
            
            // Sử dụng method có sẵn trong CartService
            cartService.removeMultipleCartItems(cartItemIds);
            System.out.println("Successfully removed " + cartItemIds.size() + " cart items");
        } catch (Exception e) {
            System.out.println("Warning: Could not remove cart items: " + e.getMessage());
            // Don't throw exception to avoid rollback
        }
    }
    
    // Helper method to create order in transaction
    @Transactional
    public ResponseEntity<?> createOrderInTransaction(OrderRequest orderRequest, AppUser user) {
        // Lấy cart items được chọn
        List<CartItem> selectedCartItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;
        
        for (Long itemId : orderRequest.getSelectedItemIds()) {
            CartItem cartItem = cartService.getCartItemById(itemId);
            if (cartItem != null && cartItem.getCart().getUser().getId().equals(user.getId())) {
                selectedCartItems.add(cartItem);
                totalPrice = totalPrice.add(cartItem.getTotalPrice());
            }
        }
        
        if (selectedCartItems.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "Không tìm thấy sản phẩm đã chọn"));
        }
        
        // Tạo đơn hàng mới
        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setTotalPrice(totalPrice);
        order.setStatus("PENDING");
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setShippingMethod(orderRequest.getShippingMethod());
        order.setPhoneNumber(orderRequest.getPhoneNumber());
        order.setPaymentMethod("COD"); // Cash on Delivery
        order.setOrderNotes(orderRequest.getOrderNotes());
        
        // Tạo OrderItems từ CartItems
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : selectedCartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getProduct().getPrice());
            orderItems.add(orderItem);
        }
        order.setOrderItems(orderItems);
        
        // Lưu đơn hàng
        CustomerOrder savedOrder = orderService.createOrder(order);
        
        // Xóa selected items khỏi giỏ hàng sau khi đơn hàng được tạo thành công
        List<Long> cartItemIds = selectedCartItems.stream()
                .map(CartItem::getId)
                .collect(Collectors.toList());
        
        System.out.println("Preparing to remove " + cartItemIds.size() + " cart items: " + cartItemIds);
        
        // Xóa cart items trong transaction riêng
        removeCartItemsInSeparateTransaction(cartItemIds);
        
        // Thêm thông tin về cart items đã xóa vào response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đặt hàng thành công");
        response.put("order", convertToOrderResponse(savedOrder, orderItems));
        response.put("removedCartItems", cartItemIds.size());
        response.put("removedCartItemIds", cartItemIds);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
    
    // Helper method to get current user from JWT
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
            }
            Optional<AppUser> userOpt = Optional.ofNullable(user);
            
            return userOpt;
            
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    // Helper method to convert OrderItem to OrderItemResponse
    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setProductId(orderItem.getProduct().getId());
        response.setProductName(orderItem.getProduct().getName());
        response.setProductImage(orderItem.getProduct().getImageUrl());
        response.setProductPrice(orderItem.getProduct().getPrice());
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setTotalPrice(orderItem.getTotalPrice());
        response.setCreatedAt(orderItem.getCreatedAt());
        // OrderItem doesn't have updatedAt field, so we'll use createdAt as updatedAt
        response.setUpdatedAt(orderItem.getCreatedAt());
        
        // Create ProductInfo object
        ProductInfo productInfo = new ProductInfo(
            orderItem.getProduct().getId(),
            orderItem.getProduct().getName(),
            orderItem.getProduct().getImageUrl(),
            orderItem.getProduct().getPrice()
        );
        response.setProduct(productInfo);
        
        return response;
    }
    
    // Helper method to convert CustomerOrder to OrderResponse
    private OrderResponse convertToOrderResponse(CustomerOrder order, List<OrderItem> orderItems) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setUserEmail(order.getUser().getEmail());
        response.setUserName(order.getUser().getFullName());
        response.setTotalPrice(order.getTotalPrice());
        response.setTotalAmount(order.getTotalPrice()); // Alias for totalPrice
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setShippingMethod(order.getShippingMethod());
        response.setPhoneNumber(order.getPhoneNumber());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setOrderNotes(order.getOrderNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setOrderDate(order.getCreatedAt()); // Alias for createdAt
        
        // Create UserInfo object
        UserInfo userInfo = new UserInfo(
            order.getUser().getId(),
            order.getUser().getFullName(),
            order.getUser().getEmail(),
            order.getUser().getUsername(),
            order.getUser().getRole()
        );
        response.setUser(userInfo);
        
        // Convert order items to DTOs
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());
        response.setOrderItems(itemResponses);
        
        return response;
    }
    
    // Tạo đơn hàng mới
    @Operation(description = "Create a new order from selected cart items (requires login)")
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest, HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập để đặt hàng"));
        }
        
        AppUser user = userOpt.get();
        
        // Validate request
        if (orderRequest.getSelectedItemIds() == null || orderRequest.getSelectedItemIds().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "Vui lòng chọn ít nhất một sản phẩm"));
        }
        
        if (orderRequest.getShippingAddress() == null || orderRequest.getShippingAddress().trim().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "Vui lòng nhập địa chỉ giao hàng"));
        }
        
        if (orderRequest.getShippingMethod() == null || orderRequest.getShippingMethod().trim().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "Vui lòng chọn phương thức giao hàng"));
        }
        
        if (orderRequest.getPhoneNumber() == null || orderRequest.getPhoneNumber().trim().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "Vui lòng nhập số điện thoại"));
        }
        
        try {
            // Tạo đơn hàng trong transaction riêng
            return createOrderInTransaction(orderRequest, user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Có lỗi xảy ra khi đặt hàng: " + e.getMessage()));
        }
    }
    
    @Operation(description = "Buy now: create an order for a single product immediately (requires login)")
    @PostMapping("/buy-now")
    public ResponseEntity<?> buyNow(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập để đặt hàng"));
        }
        AppUser user = userOpt.get();
        // Chuyển toàn bộ xử lý sang service
        return orderService.handleBuyNow(user, payload);
    }


    // Admin: Lọc đơn hàng
    @Operation(description = "Admin: Filter orders by status, ID, date, or search term (requires ADMIN role)")
    @GetMapping("/filter")
    public ResponseEntity<?> filterOrdersForAdmin(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "search", required = false) String search,
            HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        if (userOpt.isEmpty() || !"ADMIN".equals(userOpt.get().getRole())) {
            return ResponseEntity.status(403).body(Map.of("message", "Không có quyền truy cập"));
        }
        try {
            List<CustomerOrder> orders = orderService.filterOrdersForAdmin(status, orderId, startDate, endDate, search);
            List<OrderResponse> orderResponses = orders.stream()
                    .map(order -> {
                        List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
                        return convertToOrderResponse(order, orderItems);
                    })
                    .collect(Collectors.toList());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orders", orderResponses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi khi lọc đơn hàng"));
        }
    }
    
    // Lấy chi tiết đơn hàng
    @Operation(description = "Get order details by order ID (owner or ADMIN only)")
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable("orderId") Long orderId, HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        
        AppUser user = userOpt.get();
        Optional<CustomerOrder> orderOpt = orderService.getOrderById(orderId);
        
        if (!orderOpt.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng"));
        }
        
        CustomerOrder order = orderOpt.get();
        
        // Kiểm tra quyền truy cập (chỉ user sở hữu hoặc admin mới được xem)
        if (!order.getUser().getId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("message", "Không có quyền truy cập đơn hàng này"));
        }
        
        List<OrderItem> orderItems = orderService.getOrderItems(orderId);
        OrderResponse orderResponse = convertToOrderResponse(order, orderItems);
        
        return ResponseEntity.ok(orderResponse);
    }
    
    // Lấy danh sách đơn hàng của user hoặc tất cả đơn hàng cho admin
    @Operation(description = "Get all orders of the current user (requires login)")
    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders(HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        AppUser user = userOpt.get();
        // Luôn chỉ trả về đơn hàng của user hiện tại, kể cả admin
        List<CustomerOrder> orders = orderService.getOrdersByUserId(user.getId());
        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
                    return convertToOrderResponse(order, orderItems);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderResponses);
    }
    
    // Admin: Lấy tất cả đơn hàng
    @Operation(description = "Admin: Get all orders (requires ADMIN role)")
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders(HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        
        AppUser user = userOpt.get();
        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("message", "Không có quyền truy cập"));
        }
        
        List<CustomerOrder> orders = orderService.getAllOrders();
        
        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
                    return convertToOrderResponse(order, orderItems);
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(orderResponses);
    }
    
    // Admin: Cập nhật trạng thái đơn hàng
    @Operation(description = "Admin: Update order status (requires ADMIN role)")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable("orderId") Long orderId, @RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        Optional<AppUser> userOpt = getCurrentUser(httpRequest);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        AppUser user = userOpt.get();
        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("message", "Không có quyền truy cập"));
        }
        String status = request.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("message", "Trạng thái không được để trống"));
        }
        CustomerOrder updatedOrder = orderService.updateOrderStatus(orderId, status);
        if (updatedOrder == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng"));
        }
        List<OrderItem> orderItems = orderService.getOrderItems(orderId);
        OrderResponse orderResponse = convertToOrderResponse(updatedOrder, orderItems);
        // Chuẩn bị trả về lịch sử trạng thái nếu có
        // List<OrderStatusHistory> history = orderStatusHistoryRepository.findByOrderId(orderId);
        // orderResponse.setStatusHistory(history);
        return ResponseEntity.ok(orderResponse);
    }
    
    // User: Hủy đơn hàng
    @Operation(description = "Cancel an order by order ID (owner only)")
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable("orderId") Long orderId, @RequestBody(required = false) Map<String, String> request, HttpServletRequest httpRequest) {
        Optional<AppUser> userOpt = getCurrentUser(httpRequest);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        
        AppUser user = userOpt.get();
        Optional<CustomerOrder> orderOpt = orderService.getOrderById(orderId);
        
        if (!orderOpt.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng"));
        }
        
        CustomerOrder order = orderOpt.get();
        
        // Kiểm tra quyền hủy đơn hàng
        if (!order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Không có quyền hủy đơn hàng này"));
        }
        
        if (!orderService.canCancelOrder(orderId)) {
            return ResponseEntity.status(400).body(Map.of("message", "Không thể hủy đơn hàng này"));
        }
        
        // Lấy lý do hủy đơn hàng
        String reason = null;
        if (request != null && request.containsKey("reason")) {
            reason = request.get("reason");
        }
        
        CustomerOrder cancelledOrder = orderService.cancelOrder(orderId);
        
        if (cancelledOrder == null) {
            return ResponseEntity.status(400).body(Map.of("message", "Không thể hủy đơn hàng"));
        }
        
        // Log lý do hủy đơn hàng
        if (reason != null && !reason.trim().isEmpty()) {
            System.out.println("Order " + orderId + " cancelled by user " + user.getEmail() + " with reason: " + reason);
        }
        
        List<OrderItem> orderItems = orderService.getOrderItems(orderId);
        OrderResponse orderResponse = convertToOrderResponse(cancelledOrder, orderItems);
        
        return ResponseEntity.ok(orderResponse);
    }
    
    // User: Xác nhận thanh toán
    @Operation(description = "Confirm payment for an order (owner only)")
    @PutMapping("/{orderId}/confirm-payment")
    public ResponseEntity<?> confirmPayment(@PathVariable("orderId") Long orderId, HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        AppUser user = userOpt.get();
        Optional<CustomerOrder> orderOpt = orderService.getOrderById(orderId);
        if (!orderOpt.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng"));
        }
        CustomerOrder order = orderOpt.get();
        if (!order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Không có quyền xác nhận đơn hàng này"));
        }
        if (!orderService.canConfirmPayment(orderId)) {
            return ResponseEntity.status(400).body(Map.of("message", "Không thể xác nhận thanh toán cho đơn hàng này"));
        }
        CustomerOrder confirmedOrder = orderService.confirmPayment(orderId);
        if (confirmedOrder == null) {
            return ResponseEntity.status(400).body(Map.of("message", "Không thể xác nhận thanh toán"));
        }
        List<OrderItem> orderItems = orderService.getOrderItems(orderId);
        OrderResponse orderResponse = convertToOrderResponse(confirmedOrder, orderItems);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Đã xác nhận thanh toán!",
            "order", orderResponse
        ));
    }
    
    // Admin: Lấy tất cả đơn hàng (endpoint cho admin-orders.html)
    @Operation(description = "Admin: Get all orders for admin-orders.html (requires ADMIN role)")
    @GetMapping("/admin")
    public ResponseEntity<?> getAdminOrders(HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        
        AppUser user = userOpt.get();
        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("message", "Không có quyền truy cập"));
        }
        
        List<CustomerOrder> orders = orderService.getAllOrders();
        
        List<OrderResponse> orderResponses = orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderService.getOrderItems(order.getId());
                    return convertToOrderResponse(order, orderItems);
                })
                .collect(Collectors.toList());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("orders", orderResponses);
        
        return ResponseEntity.ok(response);
    }
    
    // Admin: Lấy thống kê đơn hàng
    @Operation(description = "Admin: Get order statistics (requires ADMIN role)")
    @GetMapping("/stats")
    public ResponseEntity<?> getOrderStats(HttpServletRequest request) {
        Optional<AppUser> userOpt = getCurrentUser(request);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        
        AppUser user = userOpt.get();
        if (!"ADMIN".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("message", "Không có quyền truy cập"));
        }
        
        try {
            List<CustomerOrder> allOrders = orderService.getAllOrders();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", allOrders.size());
            stats.put("pending", (int) allOrders.stream().filter(o -> "PENDING".equals(o.getStatus())).count());
            stats.put("shipping", (int) allOrders.stream().filter(o -> "SHIPPING".equals(o.getStatus())).count());
            stats.put("completed", (int) allOrders.stream().filter(o -> "COMPLETED".equals(o.getStatus())).count());
            stats.put("cancelled", (int) allOrders.stream().filter(o -> "CANCELLED".equals(o.getStatus())).count());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Lỗi khi lấy thống kê"));
        }
    }

}