package com.example.FoodHKD.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Integer userID;
    @Column(name = "Username")
    private String username;
    @Column(name = "PasswordHash")
    private String passwordHash;
    @Column(name = "FullName")
    private String fullName;
    @Column(name = "Role")
    private String role;

    private String avatar;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnore

    private List<Order> orders;

    @OneToMany(mappedBy = "issuedBy")
    @JsonIgnore

    private List<Invoice> invoices;

    @OneToMany(mappedBy = "employee")
    @JsonIgnore

    private List<TableEntity> tableEntities;



    @OneToMany(mappedBy = "createdBy")
    @JsonIgnore

    private List<InventoryLog> inventoryLogs;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "PasswordHash", length = 255, nullable = false)
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public List<TableEntity> getTableEntities() {
        return tableEntities;
    }

    public void setTableEntities(List<TableEntity> tableEntities) {
        this.tableEntities = tableEntities;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

    public List<InventoryLog> getInventoryLogs() {
        return inventoryLogs;
    }

    public void setInventoryLogs(List<InventoryLog> inventoryLogs) {
        this.inventoryLogs = inventoryLogs;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
