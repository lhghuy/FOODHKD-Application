package com.example.FoodHKD.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.FoodHKD.model.OnlineOrderItem;

/**
 * Repository for OnlineOrderItem entity
 */
@Repository
public interface OnlineOrderItemRepository extends JpaRepository<OnlineOrderItem, Integer> {
    
    /**
     * Find all items by order ID
     */
    List<OnlineOrderItem> findByOnlineOrderOrderId(Integer orderId);
}
