package com.example.FoodHKD.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.FoodHKD.model.OnlineOrder;
import com.example.FoodHKD.repository.OnlineOrderRepository;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportAdminController {

    @Autowired
    private OnlineOrderRepository onlineOrderRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        try {
            // Convert LocalDate to LocalDateTime (start and end of day)
            LocalDateTime startDateTime = dateFrom.atStartOfDay();
            LocalDateTime endDateTime = dateTo.atTime(23, 59, 59);

            // Get all orders in date range
            List<OnlineOrder> orders = onlineOrderRepository.findByCreatedAtBetween(startDateTime, endDateTime);

            // Calculate 4 main metrics
            int totalOrders = orders.size();
            
            int totalCompletedOrders = (int) orders.stream()
                .filter(o -> "Completed".equalsIgnoreCase(o.getStatus()) || "Delivered".equalsIgnoreCase(o.getStatus()))
                .count();
            
            double totalSalesCompleted = orders.stream()
                .filter(o -> "Completed".equalsIgnoreCase(o.getStatus()) || "Delivered".equalsIgnoreCase(o.getStatus()))
                .mapToDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue() : 0)
                .sum();
            
            double averageOrderValue = totalCompletedOrders > 0 ? totalSalesCompleted / totalCompletedOrders : 0.0;

            // Calculate daily sales for chart
            Map<String, Double> dailySales = new TreeMap<>();
            for (LocalDate date = dateFrom; !date.isAfter(dateTo); date = date.plusDays(1)) {
                LocalDateTime dayStart = date.atStartOfDay();
                LocalDateTime dayEnd = date.atTime(23, 59, 59);
                
                double daySales = orders.stream()
                    .filter(o -> !o.getCreatedAt().isBefore(dayStart) && !o.getCreatedAt().isAfter(dayEnd))
                    .filter(o -> "Completed".equalsIgnoreCase(o.getStatus()) || "Delivered".equalsIgnoreCase(o.getStatus()))
                    .mapToDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue() : 0)
                    .sum();
                
                dailySales.put(date.toString(), daySales);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of(
                "dateFrom", dateFrom,
                "dateTo", dateTo,
                "totalOrders", totalOrders,
                "totalCompletedOrders", totalCompletedOrders,
                "totalSalesCompleted", totalSalesCompleted,
                "averageOrderValue", averageOrderValue,
                "dailySales", dailySales
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Lỗi khi tải báo cáo: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
