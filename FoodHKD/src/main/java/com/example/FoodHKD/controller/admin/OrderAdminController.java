package com.example.FoodHKD.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.Order;
import com.example.FoodHKD.repository.OrderDetailRepository;
import com.example.FoodHKD.repository.OrderRepository;

@RestController
@RequestMapping("/admin/orders")
public class OrderAdminController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping
    public String getAllOrders(Model model) {
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("pageTitle", "Quản lý Hóa đơn");
        return "ordermanagement";
    }

    @GetMapping("/{id}")
    public String getOrderDetail(@PathVariable("id") Integer id, Model model) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/admin/orders";
        }

        return "orderdetail";
    }
}
