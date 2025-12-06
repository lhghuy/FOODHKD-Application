package com.example.FoodHKD.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.FoodHKD.model.Order;
import com.example.FoodHKD.model.OrderDetail; 

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrder(Order order);
    
    // New method for report generation - top selling items
    @Query("SELECT od.foodItem.name, SUM(od.quantity) FROM OrderDetail od " +
           "WHERE od.order.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY od.foodItem.name " +
           "ORDER BY SUM(od.quantity) DESC")
    List<Object[]> getTopSellingItemsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
