package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.service.ProductService;
import com.beui.ecommerceApp.service.UserService;
import com.beui.ecommerceApp.service.AuthService;
import com.beui.ecommerceApp.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class WebControllerTest {
    @Mock private ProductService productService;
    @Mock private UserService userService;
    @Mock private AuthService authService;
    @Mock private JwtService jwtService;
    @Mock private Model model;
    @Mock private HttpServletRequest request;
    @Mock private RedirectAttributes redirectAttributes;
    @InjectMocks private WebController webController;

    @BeforeEach
    void setUp() { MockitoAnnotations.openMocks(this); }

    @Test void testHome() { assertEquals("index", webController.home(model, request)); }
    @Test void testProducts() { assertEquals("products", webController.products(model, request)); }
    @Test void testLogin() { assertEquals("redirect:/", webController.login()); }
    @Test void testRegister() { assertEquals("register", webController.register()); }
    @Test void testRegisterUser() {
        when(authService.webRegister(any(), any(), any(), any(), any(), any())).thenReturn("redirect:/");
        assertEquals("redirect:/", webController.registerUser("a","b","c","d","e",redirectAttributes));
    }
    @Test void testAdmin() { assertEquals("admin", webController.admin()); }
    @Test void testOrdersPage() { assertEquals("order", webController.orders()); }
    @Test void testDashboard() { assertEquals("dashboard", webController.dashboard()); }
    @Test void testTest() { assertEquals("Application is running!", webController.test()); }
    @Test void testCart() { assertEquals("cart", webController.cart(model, request)); }
    @Test void testOrderSuccess() { assertEquals("order-success", webController.orderSuccess(model, request)); }
    @Test void testOrders() { assertEquals("order", webController.orders(model, request)); }
    @Test void testAdminOrders() { assertEquals("admin-orders", webController.adminOrders(model, request)); }
    @Test void testAdminProducts() { assertEquals("admin-products", webController.adminProducts(model, request)); }
    @Test void testTestHeaderDropdown() { assertEquals("test-header-dropdown", webController.testHeaderDropdown(model, request)); }
    @Test void testTeaGuideBenefit() { assertEquals("tea-guide-benefit", webController.teaGuideBenefit("benefit", model, request)); }
} 