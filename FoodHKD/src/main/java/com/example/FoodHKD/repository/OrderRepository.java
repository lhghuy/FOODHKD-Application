package com.example.FoodHKD.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.FoodHKD.model.Order;
import com.example.FoodHKD.model.User;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByStatus(String status);
    List<Order> findByCreatedBy(User user);
    
    // New methods for report generation
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Long countOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate GROUP BY o.status")
    List<Object[]> getOrderStatusBreakdown(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Calculate total sales from order details using JPQL
    @Query("SELECT SUM(od.quantity * od.priceAtOrderTime) FROM Order o " +
           "JOIN o.orderDetails od " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesFromOrders(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Get daily sales from orders using JPQL and CAST function for MS SQL Server
    @Query("SELECT CAST(o.createdAt AS DATE), SUM(od.quantity * od.priceAtOrderTime) FROM Order o " +
           "JOIN o.orderDetails od " +
           "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(o.createdAt AS DATE) " +
           "ORDER BY CAST(o.createdAt AS DATE)")
    List<Object[]> getDailySalesFromOrders(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Count orders that have order details (completed orders)
    @Query("SELECT COUNT(DISTINCT o) FROM Order o JOIN o.orderDetails od WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Long countCompletedOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
