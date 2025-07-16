package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.entity.Product;
import com.beui.ecommerceApp.service.AuthService;
import com.beui.ecommerceApp.service.JwtService;
import com.beui.ecommerceApp.service.ProductService;
import com.beui.ecommerceApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Web Pages", description = "Controllers for rendering web pages and handling web navigation for the GreenTea App.")
@Controller
public class WebController {

    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtService jwtService;

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("allProducts", products);

        List<Product> bestSellers = productService.getBestSellers();

        // Add user info from JWT
        addUserInfoToModel(model, request);
        
        // Handle flash messages from redirect
        if (model.containsAttribute("success")) {
            model.addAttribute("success", model.getAttribute("success"));
        }
        if (model.containsAttribute("error")) {
            model.addAttribute("error", model.getAttribute("error"));
        }
        
        // Handle logout message
        String logoutParam = request.getParameter("logout");
        if ("true".equals(logoutParam)) {
            model.addAttribute("success", "Đăng xuất thành công!");
        }
        
        // Handle error message
        String errorParam = request.getParameter("error");
        if ("true".equals(errorParam)) {
            model.addAttribute("error", "Đăng nhập thất bại. Vui lòng thử lại.");
        }
        
        // Handle expired session message
        String expiredParam = request.getParameter("expired");
        if ("true".equals(expiredParam)) {
            model.addAttribute("warning", "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
        }
        
        // Handle access denied message
        String accessDeniedParam = request.getParameter("access_denied");
        if ("true".equals(accessDeniedParam)) {
            model.addAttribute("error", "Bạn không có quyền truy cập trang này. Vui lòng đăng nhập với tài khoản có quyền phù hợp.");
        }
        
        return "index";
    }

    @GetMapping("/products")
    public String products(Model model, HttpServletRequest request) {
        System.out.println("🛍️ Products page requested");
        List<Product> products = productService.getAllProducts();
        System.out.println("📦 Products found: " + products.size());
        model.addAttribute("products", products);
        
        // Add user info from JWT
        addUserInfoToModel(model, request);
        
        return "products";
    }

    @GetMapping("/login")
    public String login() {
        // Redirect to home page since we don't have a separate login page
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
    
    @PostMapping("/register")
    public String registerUser(@RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              @RequestParam(required = false) String agreeTerms,
                              RedirectAttributes redirectAttributes) {
        return authService.webRegister(fullName, email, password, confirmPassword, agreeTerms, redirectAttributes);
    }

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/orders-page")
    public String orders() {
        return "order";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/test")
    public String test() {
        return "Application is running!";
    }

    @GetMapping("/debug-auth")
    public String debugAuth() {
        return "debug_auth";
    }

    @GetMapping("/test-login")
    public String testLogin() {
        return "test_login_debug";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable("id") Long id, Model model, HttpServletRequest request) {

        try {
            Optional<Product> productOpt = productService.getProductById(id);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                model.addAttribute("product", product);
                
                // Add user info from JWT
                addUserInfoToModel(model, request);
                
                return "product-detail";
            } else {
                System.out.println("❌ Product not found for ID: " + id);
                // Redirect to home page with error message
                return "redirect:/?error=product_not_found";
            }
        } catch (Exception e) {
            System.out.println("💥 Error getting product with ID " + id + ": " + e.getMessage());
            e.printStackTrace();
            return "redirect:/?error=server_error";
        }
    }

    @GetMapping("/api/debug/products")
    public String debugProducts() {
        System.out.println("🔍 Debug API called");
        List<Product> products = productService.getAllProducts();
        StringBuilder sb = new StringBuilder();
        sb.append("=== DEBUG PRODUCTS ===\n");
        sb.append("Total products: ").append(products.size()).append("\n\n");
        
        if (products.isEmpty()) {
            sb.append("❌ No products found in database!\n");
        } else {
            sb.append("✅ Products found:\n");
            products.forEach(p -> {
                sb.append("  - ID: ").append(p.getId())
                  .append(", Name: ").append(p.getName())
                  .append(", Price: ").append(p.getPrice())
                  .append(", Category: ").append(p.getCategory() != null ? p.getCategory().getName() : "NULL")
                  .append("\n");
            });
        }
        
        return sb.toString();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // This endpoint is now handled by AuthController with JWT cookie
        return ResponseEntity.ok().body(Map.of("message", "Logout handled by AuthController"));
    }

    @GetMapping("/cart")
    public String cart(Model model, HttpServletRequest request) {
        // Add user info from JWT
        addUserInfoToModel(model, request);
        return "cart";
    }

    @GetMapping("/order-success")
    public String orderSuccess(Model model, HttpServletRequest request) {
        // Add user info from JWT
        addUserInfoToModel(model, request);
        return "order-success";
    }
    
    @GetMapping("/orders")
    public String orders(Model model, HttpServletRequest request) {
        addUserInfoToModel(model, request);
        return "order";
    }
    
    @GetMapping("/admin/orders")
    public String adminOrders(Model model, HttpServletRequest request) {
        addUserInfoToModel(model, request);
        return "admin-orders";
    }

    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminProducts(Model model, HttpServletRequest request) {
        addUserInfoToModel(model, request);
        return "admin-products";
    }

    @GetMapping("/test-header-dropdown")
    public String testHeaderDropdown(Model model, HttpServletRequest request) {
        // Add user info from JWT
        addUserInfoToModel(model, request);
        return "test-header-dropdown";
    }

    @GetMapping("/tea-guide/{benefitType}")
    public String teaGuideBenefit(@PathVariable("benefitType") String benefitType, Model model, HttpServletRequest request) {
        model.addAttribute("benefitType", benefitType);
        // Có thể add user info nếu cần
        addUserInfoToModel(model, request);
        return "tea-guide-benefit";
    }
    
    /**
     * Helper method to extract user info from JWT and add to model
     */
    private void addUserInfoToModel(Model model, HttpServletRequest request) {
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
            
            if (token != null) {
                // Validate JWT token
                String email = jwtService.extractUsername(token);
                if (email != null) {
                    // Get user details
                    ResponseEntity<?> userResponse = userService.getUserByEmail(email);
                    AppUser user = null;
                    if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() instanceof AppUser) {
                        user = (AppUser) userResponse.getBody();
                    }
                    Optional<AppUser> userOpt = Optional.ofNullable(user);
                    if (userOpt.isPresent()) {
                        AppUser userEntity = userOpt.get();
                        model.addAttribute("currentUser", userEntity);
                        model.addAttribute("isAuthenticated", true);
                        System.out.println("✅ User authenticated: " + userEntity.getUsername());
                        return;
                    }
                }
            }
            
            // No valid JWT token found
            model.addAttribute("isAuthenticated", false);
            
        } catch (Exception e) {
            System.out.println("💥 Error processing JWT: " + e.getMessage());
            model.addAttribute("isAuthenticated", false);
        }
    }
} 