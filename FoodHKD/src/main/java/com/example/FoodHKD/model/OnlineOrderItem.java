package com.example.FoodHKD.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Online Order Item Model - Lưu trữ items trong đơn hàng online
 * Table: online_order_items
 */
@Entity
@Table(name = "online_order_items")
public class OnlineOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer itemId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private OnlineOrder onlineOrder;

    private Integer foodId;
    private String foodName;
    private Integer quantity;
    private BigDecimal priceAtOrderTime;

    // Status: Available, Cancelled
    private String status = "Available";

    // Reason for cancellation if cancelled
    private String cancelReason;

    // ⭐ Image URL của món ăn tại thời điểm đặt
    private String imageUrl;

    // ⭐ Default constructor (required by Hibernate/JPA)
    public OnlineOrderItem() {
    }

    // ⭐ Constructor đầy đủ với imageUrl
    public OnlineOrderItem(Integer foodId, String foodName, Integer quantity, BigDecimal priceAtOrderTime, String imageUrl) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.quantity = quantity;
        this.priceAtOrderTime = priceAtOrderTime;
        this.imageUrl = imageUrl;
    }

    // Getters & Setters
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public OnlineOrder getOnlineOrder() {
        return onlineOrder;
    }

    public void setOnlineOrder(OnlineOrder onlineOrder) {
        this.onlineOrder = onlineOrder;
    }

    public Integer getFoodId() {
        return foodId;
    }

    public void setFoodId(Integer foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtOrderTime() {
        return priceAtOrderTime;
    }

    public void setPriceAtOrderTime(BigDecimal priceAtOrderTime) {
        this.priceAtOrderTime = priceAtOrderTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
