package com.example.FoodHKD.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.FoodHKD.model.ReportDTO;
import com.example.FoodHKD.repository.InvoiceRepository;
import com.example.FoodHKD.repository.OrderDetailRepository;
import com.example.FoodHKD.repository.OrderRepository;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public ReportDTO getReport(LocalDate dateFrom, LocalDate dateTo) {
        // Convert LocalDate to LocalDateTime for database queries
        LocalDateTime startDateTime = dateFrom.atStartOfDay();
        LocalDateTime endDateTime = dateTo.atTime(23, 59, 59);

        ReportDTO report = new ReportDTO(dateFrom, dateTo);

        // Get total sales from orders (calculated from order details)
        BigDecimal totalSales = orderRepository.getTotalSalesFromOrders(startDateTime, endDateTime);
        report.setTotalSales(totalSales != null ? totalSales : BigDecimal.ZERO);

        // Get total number of orders
        Long totalOrdersCount = orderRepository.countOrdersByDateRange(startDateTime, endDateTime);
        report.setTotalOrders(totalOrdersCount != null ? totalOrdersCount.intValue() : 0);

        // Get total number of completed orders (orders with order details)
        Long completedOrdersCount = orderRepository.countCompletedOrdersByDateRange(startDateTime, endDateTime);
        report.setTotalInvoices(completedOrdersCount != null ? completedOrdersCount.intValue() : 0);

        // Calculate average order value based on completed orders
        if (report.getTotalInvoices() > 0 && report.getTotalSales().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal averageOrderValue = report.getTotalSales()
                .divide(new BigDecimal(report.getTotalInvoices()), 2, RoundingMode.HALF_UP);
            report.setAverageOrderValue(averageOrderValue);
        } else {
            report.setAverageOrderValue(BigDecimal.ZERO);
        }

        // Get order status breakdown
        List<Object[]> orderStatusData = orderRepository.getOrderStatusBreakdown(startDateTime, endDateTime);
        Map<String, Integer> orderStatusBreakdown = new HashMap<>();
        for (Object[] row : orderStatusData) {
            String status = (String) row[0];
            // Handle different numeric types from MS SQL Server
            Number countNumber = (Number) row[1];
            Integer count = countNumber.intValue();
            orderStatusBreakdown.put(status, count);
        }
        report.setOrderStatusBreakdown(orderStatusBreakdown);

        // Get daily sales breakdown from orders
        List<Object[]> dailySalesData = orderRepository.getDailySalesFromOrders(startDateTime, endDateTime);
        Map<String, BigDecimal> dailySales = new LinkedHashMap<>();
        
        for (Object[] row : dailySalesData) {
            try {
                // Handle different date formats from MS SQL Server
                String dateStr;
                if (row[0] instanceof Date) {
                    // SQL Date object
                    Date sqlDate = (Date) row[0];
                    dateStr = sqlDate.toLocalDate().toString();
                } else if (row[0] instanceof LocalDate) {
                    // LocalDate object
                    dateStr = row[0].toString();
                } else {
                    // String representation
                    dateStr = row[0].toString();
                    // If it's in a different format, try to parse and reformat
                    if (dateStr.length() > 10) {
                        dateStr = dateStr.substring(0, 10);
                    }
                }
                
                // Handle different numeric types for amount
                BigDecimal amount;
                if (row[1] instanceof BigDecimal) {
                    amount = (BigDecimal) row[1];
                } else if (row[1] instanceof Number) {
                    amount = new BigDecimal(row[1].toString());
                } else {
                    amount = new BigDecimal(row[1].toString());
                }
                
                dailySales.put(dateStr, amount);
            } catch (Exception e) {
                // Log error but continue processing
                System.err.println("Error processing daily sales data: " + e.getMessage());
            }
        }
        report.setDailySales(dailySales);

        // Get top selling items
        List<Object[]> topSellingItemsData = orderDetailRepository.getTopSellingItemsByDateRange(startDateTime, endDateTime);
        Map<String, Integer> topSellingItems = new LinkedHashMap<>();
        
        // Limit to top 10 items
        int itemCount = 0;
        for (Object[] row : topSellingItemsData) {
            if (itemCount >= 10) break;
            
            String itemName = (String) row[0];
            // Handle different numeric types from MS SQL Server
            Number quantityNumber = (Number) row[1];
            Integer quantity = quantityNumber.intValue();
            topSellingItems.put(itemName, quantity);
            itemCount++;
        }
        report.setTopSellingItems(topSellingItems);

        return report;
    }
} 