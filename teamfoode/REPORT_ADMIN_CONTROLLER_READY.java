package com.example.FoodHKD.controller.admin;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.OnlineOrder;
import com.example.FoodHKD.model.User;
import com.example.FoodHKD.repository.OnlineOrderRepository;
import com.example.FoodHKD.service.UserService;

/**
 * REST API Controller for Report functionality
 * 
 * Endpoints:
 * GET /api/admin/reports?dateFrom=2024-12-01&dateTo=2024-12-05
 * 
 * Returns report data including:
 * - Total sales and orders
 * - Completed orders count
 * - Average order value
 * - Order status breakdown
 * - Daily sales breakdown
 * - Top selling items
 */
@RestController
@RequestMapping("/api/admin/reports")
public class ReportAdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OnlineOrderRepository orderRepository;

    /**
     * Get report data for a date range
     * 
     * @param dateFrom Start date (format: YYYY-MM-DD)
     * @param dateTo End date (format: YYYY-MM-DD)
     * @param principal Current authenticated user
     * @return Report data or error message
     * 
     * Example request:
     * GET /api/admin/reports?dateFrom=2024-12-01&dateTo=2024-12-05
     * 
     * Example response:
     * {
     *   "success": true,
     *   "data": {
     *     "dateFrom": "2024-12-01",
     *     "dateTo": "2024-12-05",
     *     "totalSales": 5250000.0,
     *     "totalOrders": 42,
     *     "totalCompletedOrders": 38,
     *     "averageOrderValue": 125000.0,
     *     "orderStatusBreakdown": { "Delivered": 38, "Pending": 2 },
     *     "dailySales": { "2024-12-01": 750000.0, "2024-12-02": 850000.0 },
     *     "topSellingItems": { "Phở Bò": 15, "Cơm Gà": 12 }
     *   }
     * }
     */
    @GetMapping
    public ResponseEntity<?> getReportByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            Principal principal) {
        try {
            // Verify user has permission (admin only)
            User loggedInUser = userService.getUserByUsername(principal.getName());
            if (loggedInUser == null) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", "Unauthorized access"
                ));
            }
            
            // Convert LocalDate to LocalDateTime (start and end of day)
            LocalDateTime startDateTime = dateFrom.atStartOfDay();
            LocalDateTime endDateTime = dateTo.atTime(23, 59, 59);
            
            System.out.println("📊 Fetching report from " + startDateTime + " to " + endDateTime);
            
            // Get all orders in date range
            List<OnlineOrder> orders = orderRepository.findByCreatedAtBetween(startDateTime, endDateTime);
            System.out.println("📊 Found " + orders.size() + " orders");
            
            if (orders.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "No data for the selected period",
                    "data", Map.of(
                        "dateFrom", dateFrom,
                        "dateTo", dateTo,
                        "totalSales", 0.0,
                        "totalOrders", 0,
                        "totalCompletedOrders", 0,
                        "averageOrderValue", 0.0,
                        "orderStatusBreakdown", Map.of(),
                        "dailySales", Map.of(),
                        "topSellingItems", Map.of()
                    )
                ));
            }
            
            // Calculate total sales
            double totalSales = orders.stream()
                .mapToDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue() : 0)
                .sum();
            
            long totalOrders = orders.size();
            
            // Count completed orders (Delivered or Completed status)
            long completedOrders = orders.stream()
                .filter(o -> "Delivered".equalsIgnoreCase(o.getStatus()) || 
                           "Completed".equalsIgnoreCase(o.getStatus()))
                .count();
            
            // Calculate average order value
            double averageOrderValue = totalOrders > 0 ? totalSales / totalOrders : 0;
            
            // Group orders by status
            Map<String, Long> statusBreakdown = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getStatus() != null ? o.getStatus() : "Unknown",
                    Collectors.counting()
                ));
            
            // Group orders by date and sum totals
            Map<LocalDate, Double> dailySales = orders.stream()
                .collect(Collectors.groupingBy(
                    o -> o.getCreatedAt().toLocalDate(),
                    Collectors.summingDouble(o -> o.getTotal() != null ? o.getTotal().doubleValue() : 0)
                ));
            
            // Get top 10 selling items
            Map<String, Long> topSellingItems = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(
                    item -> item.getFoodName(),
                    Collectors.summingLong(item -> item.getQuantity() != null ? item.getQuantity().longValue() : 0)
                ))
                .entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            
            // Build response map
            Map<String, Object> reportData = new HashMap<>();
            reportData.put("dateFrom", dateFrom);
            reportData.put("dateTo", dateTo);
            reportData.put("totalSales", totalSales);
            reportData.put("totalOrders", totalOrders);
            reportData.put("totalCompletedOrders", completedOrders);
            reportData.put("averageOrderValue", averageOrderValue);
            reportData.put("orderStatusBreakdown", statusBreakdown);
            reportData.put("dailySales", dailySales);
            reportData.put("topSellingItems", topSellingItems);
            
            System.out.println("✅ Report generated successfully");
            System.out.println("   Total Sales: " + totalSales);
            System.out.println("   Total Orders: " + totalOrders);
            System.out.println("   Completed: " + completedOrders);
            System.out.println("   Average: " + averageOrderValue);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", reportData
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error generating report: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "Failed to generate report",
                "message", e.getMessage()
            ));
        }
    }
}
