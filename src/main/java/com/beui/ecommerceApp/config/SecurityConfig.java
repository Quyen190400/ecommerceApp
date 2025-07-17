package com.beui.ecommerceApp.config;

import com.beui.ecommerceApp.filter.JwtFilter;
import com.beui.ecommerceApp.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/swagger/**").permitAll()
                    .requestMatchers("/", "/login", "/register", "/test", "/css/**", "/js/**", "/images/**", "/app/**", "/api/upload/image", "/api/upload/image/custom", "/api/upload/image/url", "/auth/create-admin", "/auth/check-auth").permitAll()
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/products","/product/**").permitAll()
                    .requestMatchers("/tea-guide/**").permitAll()
                    .requestMatchers("/orders-page").permitAll()
                    .requestMatchers("/orders").authenticated()
                    .requestMatchers("/api/cart/**").authenticated()
                    .requestMatchers("/products/admin/**", "/orders/**").hasRole("ADMIN")
                    .requestMatchers("/admin/products").authenticated()
                    .requestMatchers("/api/users/me").authenticated()
                    .requestMatchers("/api/orders/my-orders").hasAnyRole("CUSTOMER", "ADMIN")
                    .requestMatchers("/api/orders/me").hasAnyRole("CUSTOMER", "ADMIN")
                    .requestMatchers("/api/orders").hasAnyRole("CUSTOMER", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/orders/*").hasAnyRole("CUSTOMER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/orders/*/cancel").hasAnyRole("CUSTOMER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/orders/*/confirm-payment").hasAnyRole("CUSTOMER", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/orders/*/status").hasRole("ADMIN")
                    .requestMatchers("/uploads/images/**").permitAll()
                    .requestMatchers("/api/products/benefit/**").permitAll()
                    .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/")
                .defaultSuccessUrl("/", true)
                .failureUrl("/?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    // Check if it's an API request
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"message\":\"Vui lòng đăng nhập để thực hiện thao tác này\"}");
                    } else {
                        // For non-API requests, redirect to home page with expired message
                        String redirectUrl = "/";
                        response.sendRedirect(redirectUrl);
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // Check if it's an API request
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"message\":\"Bạn không có quyền truy cập tài nguyên này\"}");
                    } else {
                        // For non-API requests, redirect to home page with access denied message
                        String redirectUrl = "/?access_denied=true";
                        response.sendRedirect(redirectUrl);
                    }
                })
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
