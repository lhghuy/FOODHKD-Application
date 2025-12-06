package com.example.FoodHKD.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FoodHKD.model.OnlineOrder;

/**
 * Repository for OnlineOrder entity
 */
@Repository
public interface OnlineOrderRepository extends JpaRepository<OnlineOrder, Integer> {
    
    /**
     * Find all orders by customer ID
     */
    List<OnlineOrder> findByCustomerId(Integer customerId);
    
    /**
     * Find all orders by status
     */
    List<OnlineOrder> findByStatus(String status);
    
    /**
     * Find all orders between two dates (for reports)
     */
    List<OnlineOrder> findByCreatedAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
}
