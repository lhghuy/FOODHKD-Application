package com.example.FoodHKD.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class ReportDTO {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private BigDecimal totalSales;
    private Integer totalOrders;
    private Integer totalCompletedOrders;
    private BigDecimal averageOrderValue;
    private Map<String, Integer> orderStatusBreakdown;
    private Map<String, BigDecimal> dailySales;
    private Map<String, Integer> topSellingItems;

    public ReportDTO() {}

    public ReportDTO(LocalDate dateFrom, LocalDate dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public BigDecimal getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(BigDecimal totalSales) {
        this.totalSales = totalSales;
    }

    public Integer getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Integer totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Integer getTotalCompletedOrders() {
        return totalCompletedOrders;
    }

    public void setTotalCompletedOrders(Integer totalCompletedOrders) {
        this.totalCompletedOrders = totalCompletedOrders;
    }

    public Integer getTotalInvoices() {
        return totalCompletedOrders;
    }

    public void setTotalInvoices(Integer totalInvoices) {
        this.totalCompletedOrders = totalInvoices;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public void setAverageOrderValue(BigDecimal averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }

    public Map<String, Integer> getOrderStatusBreakdown() {
        return orderStatusBreakdown;
    }

    public void setOrderStatusBreakdown(Map<String, Integer> orderStatusBreakdown) {
        this.orderStatusBreakdown = orderStatusBreakdown;
    }

    public Map<String, BigDecimal> getDailySales() {
        return dailySales;
    }

    public void setDailySales(Map<String, BigDecimal> dailySales) {
        this.dailySales = dailySales;
    }

    public Map<String, Integer> getTopSellingItems() {
        return topSellingItems;
    }

    public void setTopSellingItems(Map<String, Integer> topSellingItems) {
        this.topSellingItems = topSellingItems;
    }
} 