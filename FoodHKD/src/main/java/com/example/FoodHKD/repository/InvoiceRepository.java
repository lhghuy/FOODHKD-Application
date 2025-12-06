package com.example.FoodHKD.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.FoodHKD.model.Invoice;
import com.example.FoodHKD.model.Order;

public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    Optional<Invoice> findByOrder(Order order);
    
    
    List<Invoice> findByIssuedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.issuedAt BETWEEN :startDate AND :endDate")
    Long countInvoicesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.issuedAt BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSalesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query(value = "SELECT CAST(i.issued_at AS DATE) as sale_date, SUM(i.total_amount) as daily_total " +
                   "FROM invoices i " +
                   "WHERE i.issued_at BETWEEN :startDate AND :endDate " +
                   "GROUP BY CAST(i.issued_at AS DATE) " +
                   "ORDER BY CAST(i.issued_at AS DATE)", nativeQuery = true)
    List<Object[]> getDailySalesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
