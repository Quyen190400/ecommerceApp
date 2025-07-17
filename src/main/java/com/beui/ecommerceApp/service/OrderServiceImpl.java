package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.dto.OrderRequest;
import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.entity.CustomerOrder;
import com.beui.ecommerceApp.entity.OrderItem;
import com.beui.ecommerceApp.entity.Product;
import com.beui.ecommerceApp.repository.ProductRepository;
import com.beui.ecommerceApp.repository.CustomerOrderRepository;
import com.beui.ecommerceApp.repository.OrderItemRepository;
import com.beui.ecommerceApp.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private CustomerOrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private AppUserRepository userRepository;

    @Override
    public List<CustomerOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<CustomerOrder> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<CustomerOrder> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<CustomerOrder> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public CustomerOrder createOrder(CustomerOrder order) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (OrderItem item : order.getOrderItems()) {
            totalPrice = totalPrice.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalPrice(totalPrice);
        CustomerOrder savedOrder = orderRepository.save(order);
        for (OrderItem item : order.getOrderItems()) {
            item.setOrder(savedOrder);
            orderItemRepository.save(item);
        }
        return savedOrder;
    }

    @Override
    @Transactional
    public CustomerOrder updateOrderStatus(Long orderId, String status) {
        Optional<CustomerOrder> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            CustomerOrder order = optionalOrder.get();
            if ("PENDING".equals(order.getStatus()) && "SHIPPING".equals(status)) {
                List<OrderItem> items = getOrderItems(orderId);
                for (OrderItem item : items) {
                    Product product = item.getProduct();
                    int newStock = product.getStockQuantity() - item.getQuantity();
                    if (newStock >= 0) {
                        product.setStockQuantity(newStock);
                        productRepository.save(product);
                    }
                }
            }
            if (!order.getStatus().equals(status)) {
                // TODO: Lưu vào bảng audit log nếu cần
            }
            order.setStatus(status);
            order.setUpdatedAt(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
            if ("COMPLETED".equals(status)) {
                List<OrderItem> items = getOrderItems(orderId);
                for (OrderItem item : items) {
                    Product product = item.getProduct();
                    int newSold = (product.getSoldCount() != null ? product.getSoldCount() : 0) + item.getQuantity();
                    product.setSoldCount(newSold);
                    productRepository.save(product);
                }
            }
            return orderRepository.save(order);
        }
        return null;
    }

    @Override
    @Transactional
    public CustomerOrder cancelOrder(Long orderId) {
        Optional<CustomerOrder> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            CustomerOrder order = optionalOrder.get();
            if ("PENDING".equals(order.getStatus())) {
                order.setStatus("CANCELLED");
                return orderRepository.save(order);
            }
        }
        return null;
    }

    @Override
    @Transactional
    public CustomerOrder confirmPayment(Long orderId) {
        return updateOrderStatus(orderId, "COMPLETED");
    }

    @Override
    @Transactional
    public CustomerOrder createBuyNowOrder(AppUser user, Long productId, Integer quantity, OrderRequest orderRequest) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) return null;
        Product product = productOpt.get();
        if (product.getStockQuantity() < quantity) return null;
        CustomerOrder order = new CustomerOrder();
        order.setUser(user);
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setShippingMethod(orderRequest.getShippingMethod());
        order.setPhoneNumber(orderRequest.getPhoneNumber());
        order.setOrderNotes(orderRequest.getOrderNotes());
        order.setStatus("PENDING");
        order.setCreatedAt(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
        order.setUpdatedAt(java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")));
        // Tạo OrderItem
        OrderItem item = new OrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        // Tính tổng tiền
        java.math.BigDecimal total = product.getPrice().multiply(java.math.BigDecimal.valueOf(quantity));
        order.setTotalPrice(total);
        // Lưu order trước để lấy ID
        CustomerOrder savedOrder = orderRepository.save(order);
        item.setOrder(savedOrder);
        orderItemRepository.save(item);
        java.util.List<OrderItem> items = new java.util.ArrayList<>();
        items.add(item);
        savedOrder.setOrderItems(items);
        return savedOrder;
    }

    @Override
    public OrderItem addItemToOrder(Long orderId, Long productId, Integer quantity) {
        Optional<CustomerOrder> optionalOrder = orderRepository.findById(orderId);
        Optional<Product> optionalProduct = productRepository.findById(productId);
        if (optionalOrder.isPresent() && optionalProduct.isPresent()) {
            CustomerOrder order = optionalOrder.get();
            Product product = optionalProduct.get();
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());
            return orderItemRepository.save(orderItem);
        }
        return null;
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Override
    public BigDecimal calculateOrderTotal(Long orderId) {
        List<OrderItem> items = getOrderItems(orderId);
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean deleteOrder(Long id) {
        Optional<CustomerOrder> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {
            orderRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean canCancelOrder(Long orderId) {
        Optional<CustomerOrder> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            CustomerOrder order = optionalOrder.get();
            return "PENDING".equals(order.getStatus());
        }
        return false;
    }

    @Override
    public boolean canConfirmPayment(Long orderId) {
        Optional<CustomerOrder> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isPresent()) {
            CustomerOrder order = optionalOrder.get();
            return "SHIPPING".equals(order.getStatus());
        }
        return false;
    }

    @Override
    public org.springframework.http.ResponseEntity<?> handleBuyNow(AppUser user, java.util.Map<String, Object> payload) {
        // Validate
        Long productId = null;
        Integer quantity = null;
        try {
            productId = Long.valueOf(payload.getOrDefault("productId", 0).toString());
            quantity = Integer.valueOf(payload.getOrDefault("quantity", 1).toString());
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Dữ liệu sản phẩm không hợp lệ"));
        }
        String shippingAddress = (String) payload.getOrDefault("shippingAddress", "");
        String shippingMethod = (String) payload.getOrDefault("shippingMethod", "");
        String phoneNumber = (String) payload.getOrDefault("phoneNumber", "");
        String orderNotes = (String) payload.getOrDefault("orderNotes", "");
        if (productId == 0 || quantity < 1) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Sản phẩm không hợp lệ"));
        }
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Vui lòng nhập địa chỉ giao hàng"));
        }
        if (shippingMethod == null || shippingMethod.trim().isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Vui lòng chọn phương thức giao hàng"));
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("message", "Vui lòng nhập số điện thoại"));
        }
        try {
            // Tạo đơn hàng cho 1 sản phẩm
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setShippingAddress(shippingAddress);
            orderRequest.setShippingMethod(shippingMethod);
            orderRequest.setPhoneNumber(phoneNumber);
            orderRequest.setOrderNotes(orderNotes);
            orderRequest.setSelectedItemIds(null); // Không dùng
            // Gọi service tạo đơn hàng buy-now
            CustomerOrder order = createBuyNowOrder(user, productId, quantity, orderRequest);
            if (order == null) {
                return org.springframework.http.ResponseEntity.status(500).body(java.util.Map.of("message", "Không thể tạo đơn hàng"));
            }
            return org.springframework.http.ResponseEntity.ok(java.util.Map.of("success", true, "orderId", order.getId()));
        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.status(500).body(java.util.Map.of("message", "Có lỗi xảy ra khi đặt hàng: " + e.getMessage()));
        }
    }

    @Override
    public List<CustomerOrder> filterOrdersByUser(Long userId, String status, String orderId, String startDate, String endDate, String search) {
        List<CustomerOrder> orders = orderRepository.findByUserId(userId);
        // Parse status thành List<String> và lọc theo nhiều trạng thái
        if (status != null && !status.trim().isEmpty()) {
            List<String> statuses = java.util.Arrays.stream(status.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            orders = orders.stream()
                .filter(o -> statuses.contains(o.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        }
        // Lọc theo orderId
        if (orderId != null && !orderId.trim().isEmpty()) {
            try {
                Long oid = Long.parseLong(orderId);
                orders = orders.stream()
                    .filter(o -> o.getId().equals(oid))
                    .collect(java.util.stream.Collectors.toList());
            } catch (NumberFormatException e) {
                // Bỏ qua nếu orderId không hợp lệ
            }
        }
        // TODO: Lọc theo startDate, endDate, search nếu cần
        return orders;
    }

    @Override
    public List<CustomerOrder> filterOrdersForAdmin(String status, String orderId, String startDate, String endDate, String search) {
        List<CustomerOrder> orders = orderRepository.findAll();
        // Parse status thành List<String> và lọc theo nhiều trạng thái
        if (status != null && !status.trim().isEmpty()) {
            List<String> statuses = java.util.Arrays.stream(status.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            orders = orders.stream()
                .filter(o -> statuses.contains(o.getStatus()))
                .collect(java.util.stream.Collectors.toList());
        }
        // Lọc theo orderId
        if (orderId != null && !orderId.trim().isEmpty()) {
            try {
                Long oid = Long.parseLong(orderId);
                orders = orders.stream()
                    .filter(o -> o.getId().equals(oid))
                    .collect(java.util.stream.Collectors.toList());
            } catch (NumberFormatException e) {
                // Bỏ qua nếu orderId không hợp lệ
            }
        }
        // TODO: Lọc theo startDate, endDate, search nếu cần
        return orders;
    }
} 