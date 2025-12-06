package com.example.FoodHKD.service;

import java.util.List;

import com.example.FoodHKD.model.Order; // Adjust the package path if necessary

public interface OrderService {
    List<Order> getAllOrders();
    Order getOrderById(Integer id);
    Order createOrder(Order order);
    Order updateOrderStatus(Integer id, String status);
    void deleteOrder(Integer id);
}
