package com.example.FoodHKD.service;

import java.util.List;

import com.example.FoodHKD.model.Order;
import com.example.FoodHKD.model.OrderDetail; // Import the Order class

public interface OrderDetailService {
    List<OrderDetail> getOrderDetailsByOrder(Order order);
    OrderDetail createOrderDetail(OrderDetail orderDetail);
    void deleteOrderDetail(Integer id);
}
