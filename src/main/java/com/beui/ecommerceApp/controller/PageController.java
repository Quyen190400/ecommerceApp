package com.beui.ecommerceApp.controller;

import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.service.UserService;
import com.beui.ecommerceApp.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    private void addUserInfoToModel(Model model, HttpServletRequest request) {
        try {
            String token = null;
            if (request.getCookies() != null) {
                for (var cookie : request.getCookies()) {
                    if ("jwt_token".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
            if (token != null) {
                String email = jwtService.extractUsername(token);
                if (email != null) {
                    var userResponse = userService.getUserByEmail(email);
                    AppUser user = null;
                    if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() instanceof AppUser) {
                        user = (AppUser) userResponse.getBody();
                    }
                    if (user != null) {
                        model.addAttribute("currentUser", user);
                        model.addAttribute("isAuthenticated", true);
                        return;
                    }
                }
            }
            model.addAttribute("isAuthenticated", false);
        } catch (Exception e) {
            model.addAttribute("isAuthenticated", false);
        }
    }

    @GetMapping("/privacy")
    public String privacy(Model model, HttpServletRequest request) {
        addUserInfoToModel(model, request);
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms(Model model, HttpServletRequest request) {
        addUserInfoToModel(model, request);
        return "terms";
    }

    @GetMapping("/return-policy")
    public String returnPolicy(Model model, HttpServletRequest request) {
        addUserInfoToModel(model, request);
        return "return-policy";
    }

    @GetMapping("/guide")
    public String guide(Model model, HttpServletRequest request) {
        addUserInfoToModel(model, request);
        return "guide";
    }
} 