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


@Entity
@Table(name = "table_details")
public class TableDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tableDetailId;

    @ManyToOne
    @JoinColumn(name = "tableID")
    @JsonIgnore

    private TableEntity table;

    @ManyToOne
    @JoinColumn(name = "foodID")
    private FoodItem foodItem;

    private Integer quantity;
    private BigDecimal totalPrice;

    public Integer getTableDetailId() {
        return tableDetailId;
    }

    public void setTableDetailId(Integer tableDetailId) {
        this.tableDetailId = tableDetailId;
    }

    public TableEntity getTable() {
        return table;
    }

    public void setTable(TableEntity table) {
        this.table = table;
    }

    public FoodItem getFoodItem() {
        return foodItem;
    }

    public void setFoodItem(FoodItem foodItem) {
        this.foodItem = foodItem;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
