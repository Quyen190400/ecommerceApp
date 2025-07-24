package com.beui.ecommerceApp.repository;

import com.beui.ecommerceApp.entity.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<AppUser> findByEmailAndActiveTrue(String email);
    @Query("SELECT u FROM AppUser u WHERE lower(u.email) LIKE lower(concat('%', :keyword, '%')) OR lower(u.fullName) LIKE lower(concat('%', :keyword, '%'))")
    Page<AppUser> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
} 