package com.beui.ecommerceApp.repository;

import com.beui.ecommerceApp.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    
    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    Optional<Cart> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Cart c WHERE c.user.email = :email")
    Optional<Cart> findByUserEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(c) FROM Cart c WHERE c.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
} 