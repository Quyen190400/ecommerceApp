package com.beui.ecommerceApp.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;
    private String email = "test@email.com";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void generateToken_and_extractUsername() {
        String token = jwtService.generateToken(email);
        String extracted = jwtService.extractUsername(token);
        assertEquals(email, extracted);
    }

    @Test
    void generateToken_withExtraClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        String token = jwtService.generateToken(claims, email);
        String extracted = jwtService.extractUsername(token);
        assertEquals(email, extracted);
        Claims allClaims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        assertEquals("ADMIN", allClaims.get("role"));
    }

    @Test
    void extractClaim() {
        String token = jwtService.generateToken(email);
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        assertEquals(email, subject);
    }

    @Test
    void isTokenValid_true() {
        String token = jwtService.generateToken(email);
        assertTrue(jwtService.isTokenValid(token, email));
    }

    @Test
    void isTokenValid_false_wrongEmail() {
        String token = jwtService.generateToken(email);
        assertFalse(jwtService.isTokenValid(token, "other@email.com"));
    }

    @Test
    void isTokenValid_false_expired() throws InterruptedException {
        // Tạo token hết hạn ngay lập tức
        Map<String, Object> claims = new HashMap<>();
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis() - 10000))
                .setExpiration(new Date(System.currentTimeMillis() - 5000))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
        assertFalse(jwtService.isTokenValid(token, email));
    }

    @Test
    void extractAllClaims_and_extractExpiration() {
        String token = jwtService.generateToken(email);
        Date exp = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody().getExpiration();
        Date extracted = jwtService.extractClaim(token, Claims::getExpiration);
        assertEquals(exp, extracted);
    }

    private Key getKey() {
        byte[] keyBytes = Base64.getDecoder().decode("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        return Keys.hmacShaKeyFor(keyBytes);
    }
} 