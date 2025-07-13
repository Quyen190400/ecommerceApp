package com.beui.ecommerceApp.repository;

import com.beui.ecommerceApp.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    
    @Query("SELECT o FROM CustomerOrder o WHERE o.user.id = :userId")
    List<CustomerOrder> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT o FROM CustomerOrder o WHERE o.status = :status")
    List<CustomerOrder> findByStatus(@Param("status") String status);
    
    @Query("SELECT o FROM CustomerOrder o WHERE o.user.id = :userId AND o.status = :status")
    List<CustomerOrder> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);
} 