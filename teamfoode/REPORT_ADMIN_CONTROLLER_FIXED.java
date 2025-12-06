package com.example.FoodHKD.controller.admin;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.FoodHKD.model.User;
import com.example.FoodHKD.model.OnlineOrder;
import com.example.FoodHKD.model.OrderDetail;
import com.example.FoodHKD.repository.OnlineOrderRepository;
import com.example.FoodHKD.service.UserService;

@RestController
@RequestMapping("/api/admin/reports")
public class ReportAdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OnlineOrderRepository orderRepository;

    /**
     * GET /api/admin/reports?dateFrom=2024-12-01&dateTo=2024-12-05
     * Returns report data for date range
     */
    @GetMapping
    public ResponseEntity<?> getReportByDateRange(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Principal principal) {
        
        try {
            // Validate dates
            if (dateFrom == null || dateTo == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "dateFrom và dateTo là bắt buộc"));
            }
            
            if (dateFrom.isAfter(dateTo)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "dateFrom phải trước dateTo"));
            }
            
            // Get logged in user (optional - can remove if reports are public)
            User loggedInUser = null;
            if (principal != null) {
                loggedInUser = userService.getUserByUsername(principal.getName());
                if (loggedInUser == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        Map.of("success", false, "message", "User không tồn tại"));
                }
            }
            
            // Convert LocalDate to LocalDateTime for query
            LocalDateTime startDateTime = dateFrom.atStartOfDay();
            LocalDateTime endDateTime = dateTo.plusDays(1).atStartOfDay();
            
            // Get all orders in date range
            List<OnlineOrder> orders = orderRepository.findByCreatedAtBetween(startDateTime, endDateTime);
            
            // Calculate report metrics
            ReportDTO report = calculateReport(orders, dateFrom, dateTo);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", report
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * Calculate report metrics from orders
     */
    private ReportDTO calculateReport(List<OnlineOrder> orders, LocalDate dateFrom, LocalDate dateTo) {
        ReportDTO report = new ReportDTO();
        
        report.setDateFrom(dateFrom.toString());
        report.setDateTo(dateTo.toString());
        
        // Total sales and order count
        double totalSales = 0;
        int totalOrders = orders.size();
        int totalCompletedOrders = 0;
        
        Map<String, Integer> statusBreakdown = new HashMap<>();
        Map<String, Double> dailySales = new TreeMap<>();
        Map<String, Integer> topItems = new HashMap<>();
        
        // Initialize daily sales for all dates in range
        LocalDate current = dateFrom;
        while (!current.isAfter(dateTo)) {
            dailySales.put(current.toString(), 0.0);
            current = current.plusDays(1);
        }
        
        // Process each order
        for (OnlineOrder order : orders) {
            totalSales += order.getTotal();
            
            // Count by status
            String status = order.getStatus() != null ? order.getStatus() : "Unknown";
            statusBreakdown.put(status, statusBreakdown.getOrDefault(status, 0) + 1);
            
            if ("Completed".equalsIgnoreCase(status) || "Delivered".equalsIgnoreCase(status)) {
                totalCompletedOrders++;
            }
            
            // Daily sales
            LocalDate orderDate = order.getCreatedAt().toLocalDate();
            String dateKey = orderDate.toString();
            dailySales.put(dateKey, dailySales.getOrDefault(dateKey, 0.0) + order.getTotal());
            
            // Top selling items
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    String itemName = detail.getFoodName(); // or detail.getFood().getName()
                    topItems.put(itemName, topItems.getOrDefault(itemName, 0) + detail.getQuantity());
                }
            }
        }
        
        // Calculate average order value
        double averageOrderValue = totalOrders > 0 ? totalSales / totalOrders : 0;
        
        // Sort top items by quantity (descending) and take top 10
        Map<String, Integer> topItemsSorted = topItems.entrySet()
            .stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(10)
            .collect(Collectors.toLinkedHashMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
        
        report.setTotalSales(totalSales);
        report.setTotalOrders(totalOrders);
        report.setTotalCompletedOrders(totalCompletedOrders);
        report.setAverageOrderValue(averageOrderValue);
        report.setOrderStatusBreakdown(statusBreakdown);
        report.setDailySales(dailySales);
        report.setTopSellingItems(topItemsSorted);
        
        return report;
    }

    /**
     * Report DTO for JSON response
     */
    public static class ReportDTO {
        private String dateFrom;
        private String dateTo;
        private Double totalSales = 0.0;
        private Integer totalOrders = 0;
        private Integer totalCompletedOrders = 0;
        private Double averageOrderValue = 0.0;
        private Map<String, Integer> orderStatusBreakdown = new HashMap<>();
        private Map<String, Double> dailySales = new HashMap<>();
        private Map<String, Integer> topSellingItems = new HashMap<>();
        
        // Getters and Setters
        public String getDateFrom() { return dateFrom; }
        public void setDateFrom(String dateFrom) { this.dateFrom = dateFrom; }
        
        public String getDateTo() { return dateTo; }
        public void setDateTo(String dateTo) { this.dateTo = dateTo; }
        
        public Double getTotalSales() { return totalSales; }
        public void setTotalSales(Double totalSales) { this.totalSales = totalSales; }
        
        public Integer getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
        
        public Integer getTotalCompletedOrders() { return totalCompletedOrders; }
        public void setTotalCompletedOrders(Integer totalCompletedOrders) { this.totalCompletedOrders = totalCompletedOrders; }
        
        public Double getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
        
        public Map<String, Integer> getOrderStatusBreakdown() { return orderStatusBreakdown; }
        public void setOrderStatusBreakdown(Map<String, Integer> orderStatusBreakdown) { this.orderStatusBreakdown = orderStatusBreakdown; }
        
        public Map<String, Double> getDailySales() { return dailySales; }
        public void setDailySales(Map<String, Double> dailySales) { this.dailySales = dailySales; }
        
        public Map<String, Integer> getTopSellingItems() { return topSellingItems; }
        public void setTopSellingItems(Map<String, Integer> topSellingItems) { this.topSellingItems = topSellingItems; }
    }
}
