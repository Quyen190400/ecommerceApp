package com.beui.ecommerceApp.service;

import com.beui.ecommerceApp.dto.UserInfo;
import com.beui.ecommerceApp.entity.AppUser;
import com.beui.ecommerceApp.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Optional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.time.format.DateTimeFormatter;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        String email = auth.getName();
        Optional<AppUser> user = userRepository.findByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> getUserById(Long id) {
        Optional<AppUser> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @Override
    public ResponseEntity<?> getAllUsers(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(userRepository.findByKeyword(keyword, pageable));
        }
        return ResponseEntity.ok(userRepository.findAll(pageable));
    }

    @Override
    public ResponseEntity<?> getUserByEmail(String email) {
        Optional<AppUser> user = userRepository.findByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<?> createUser(AppUser user) {
        return ResponseEntity.ok(userRepository.save(user));
    }

    @Override
    public ResponseEntity<?> updateUser(Long id, AppUser userDetails) {
        Optional<AppUser> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            AppUser user = optionalUser.get();
            user.setFullName(userDetails.getFullName());
            user.setEmail(userDetails.getEmail());
            user.setRole(userDetails.getRole());
            return ResponseEntity.ok(userRepository.save(user));
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<?> deleteUser(Long id) {
        Optional<AppUser> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    public Optional<AppUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    @Override
    public void setActive(Long userId, boolean active) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setActive(active);
            userRepository.save(user);
        });
    }
    @Override
    public void resetPassword(Long userId, String newPassword) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        });
    }
    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<AppUser> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean forgotPassword(String email) {
        Optional<AppUser> userOpt = userRepository.findByEmailAndActiveTrue(email);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            String defaultPassword = "TeaShop@123";
            user.setPassword(passwordEncoder.encode(defaultPassword));
            userRepository.save(user);
            // Gửi email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Green tea | Mật khẩu mới cho tài khoản của bạn");
            message.setText("Mật khẩu mới của bạn là: " + defaultPassword + "\nVui lòng đăng nhập và đổi mật khẩu ngay!");
            mailSender.send(message);
            return true;
        }
        return false;
    }

    @Override
    public void sendMail(SimpleMailMessage message) {
        mailSender.send(message);
    }
    @Override
    public Optional<AppUser> findById(Long id) {
        return userRepository.findById(id);
    }

    // New methods for profile functionality
    @Override
    public UserInfo getUserInfoByUsername(String username) {
        Optional<AppUser> userOpt = userRepository.findByEmail(username);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            UserInfo userInfo = new UserInfo();
            userInfo.setId(user.getId());
            userInfo.setFullName(user.getFullName());
            userInfo.setEmail(user.getEmail());
            userInfo.setUsername(user.getEmail()); // Set username to email for consistency
            userInfo.setPhone(user.getPhone());
            userInfo.setRole(user.getRole());
            userInfo.setActive(user.getActive());
            userInfo.setAvatarUrl(user.getAvatarUrl());
            if (user.getCreatedAt() != null) {
                userInfo.setCreatedAt(user.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            
            // Debug logging
            System.out.println("🔍 Debug getUserInfoByUsername:");
            System.out.println("  - Username param: " + username);
            System.out.println("  - User email: " + user.getEmail());
            System.out.println("  - UserInfo email: " + userInfo.getEmail());
            System.out.println("  - UserInfo username: " + userInfo.getUsername());
            
            return userInfo;
        }
        throw new RuntimeException("User not found");
    }

    @Override
    public void updateUserProfile(UserInfo userInfo) {
        Optional<AppUser> userOpt = userRepository.findById(userInfo.getId());
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            user.setFullName(userInfo.getFullName());
            user.setPhone(userInfo.getPhone());
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    @Override
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        try {
            System.out.println("🔍 Debug changePassword:");
            System.out.println("  - Username: " + username);
            System.out.println("  - Current password length: " + (currentPassword != null ? currentPassword.length() : "NULL"));
            System.out.println("  - New password length: " + (newPassword != null ? newPassword.length() : "NULL"));
            
            Optional<AppUser> userOpt = userRepository.findByEmail(username);
            if (userOpt.isPresent()) {
                AppUser user = userOpt.get();
                System.out.println("  - User found: " + user.getEmail());
                System.out.println("  - User active: " + user.getActive());
                
                if (user.getActive()) {
                    if (passwordEncoder.matches(currentPassword, user.getPassword())) {
                        user.setPassword(passwordEncoder.encode(newPassword));
                        userRepository.save(user);
                        System.out.println("✅ Password changed successfully for user: " + user.getEmail());
                        return true;
                    } else {
                        System.out.println("❌ Current password doesn't match for user: " + user.getEmail());
                        return false;
                    }
                } else {
                    System.out.println("❌ User is inactive: " + user.getEmail());
                    return false;
                }
            } else {
                System.out.println("❌ User not found: " + username);
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Error in changePassword: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateUserAvatarUrl(String username, String url) {
        Optional<AppUser> userOpt = userRepository.findByEmail(username);
        if (userOpt.isPresent()) {
            AppUser user = userOpt.get();
            user.setAvatarUrl(url);
            userRepository.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }
} 