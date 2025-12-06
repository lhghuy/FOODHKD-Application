package com.example.FoodHKD.controller.client;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.Order;
import com.example.FoodHKD.model.OrderDetail;
import com.example.FoodHKD.service.OrderDetailService;
import com.example.FoodHKD.service.OrderService;

@RestController
@RequestMapping("/client/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @PostMapping("/{orderId}/details")
public OrderDetail addItem(@PathVariable Integer orderId, @RequestBody OrderDetail detail) {
    Order order = new Order();
    order.setOrderID(orderId);
    detail.setOrder(order);
    return orderDetailService.createOrderDetail(detail);
}


    @PutMapping("/{orderId}/status")
    public Order updateStatus(@PathVariable Integer orderId, @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }
}
