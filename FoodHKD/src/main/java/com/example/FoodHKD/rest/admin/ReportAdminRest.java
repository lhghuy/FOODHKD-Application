package com.example.FoodHKD.rest.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Admin Reports
 * Endpoints: /api/admin/reports
 * 
 * DISABLED: Use ReportAdminController instead (in controller/admin package)
 */
// @RestController
// @RequestMapping("/api/admin/reports")
public class ReportAdminRest {

    /**
     * GET /api/admin/reports?dateFrom=YYYY-MM-DD&dateTo=YYYY-MM-DD - Generate report
     * Response: { "success": true, "report": { "totalRevenue": ..., "totalOrders": ..., ... } }
     */
    @GetMapping
    public ResponseEntity<?> generateReport(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fromDate = dateFrom != null ? LocalDate.parse(dateFrom, formatter) : LocalDate.now().minusMonths(1);
            LocalDate toDate = dateTo != null ? LocalDate.parse(dateTo, formatter) : LocalDate.now();
            
            // Mock report data
            Map<String, Object> report = new HashMap<>();
            
            // Revenue metrics
            report.put("totalRevenue", 1250000.0);
            report.put("totalOrders", 45);
            report.put("averageOrderValue", 27777.78);
            report.put("totalCustomers", 32);
            
            // Order status breakdown
            Map<String, Integer> ordersByStatus = new HashMap<>();
            ordersByStatus.put("COMPLETED", 40);
            ordersByStatus.put("PENDING", 3);
            ordersByStatus.put("CANCELLED", 2);
            report.put("ordersByStatus", ordersByStatus);
            
            // Payment method breakdown
            Map<String, Integer> paymentMethods = new HashMap<>();
            paymentMethods.put("CASH", 25);
            paymentMethods.put("CARD", 15);
            paymentMethods.put("TRANSFER", 5);
            report.put("paymentMethods", paymentMethods);
            
            // Top products
            Map<String, Object> topProduct = new HashMap<>();
            topProduct.put("productId", 1);
            topProduct.put("productName", "Cơm tấm");
            topProduct.put("quantity", 45);
            topProduct.put("revenue", 225000.0);
            report.put("topProduct", topProduct);
            
            // Daily revenue
            Map<String, Double> dailyRevenue = new HashMap<>();
            dailyRevenue.put("2025-11-18", 150000.0);
            dailyRevenue.put("2025-11-19", 280000.0);
            dailyRevenue.put("2025-11-20", 420000.0);
            report.put("dailyRevenue", dailyRevenue);
            
            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Report generated successfully");
            response.put("dateFrom", dateFrom);
            response.put("dateTo", dateTo);
            response.put("report", report);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/admin/reports/daily?date=YYYY-MM-DD - Get daily report
     * Response: { "success": true, "dailyReport": {...} }
     */
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyReport(@RequestParam(required = false) String date) {
        try {
            LocalDate reportDate = date != null ? 
                LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : 
                LocalDate.now();
            
            // Mock daily report data
            Map<String, Object> dailyReport = new HashMap<>();
            dailyReport.put("date", reportDate.toString());
            dailyReport.put("totalRevenue", 420000.0);
            dailyReport.put("totalOrders", 15);
            dailyReport.put("totalCustomers", 12);
            dailyReport.put("averageOrderValue", 28000.0);
            
            // Hourly breakdown
            Map<String, Double> hourlyRevenue = new HashMap<>();
            hourlyRevenue.put("10:00-11:00", 50000.0);
            hourlyRevenue.put("11:00-12:00", 80000.0);
            hourlyRevenue.put("12:00-13:00", 120000.0);
            hourlyRevenue.put("13:00-14:00", 90000.0);
            hourlyRevenue.put("14:00-15:00", 80000.0);
            dailyReport.put("hourlyRevenue", hourlyRevenue);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dailyReport", dailyReport);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }
}
