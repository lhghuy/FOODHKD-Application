package com.example.FoodHKD.service;

import com.example.FoodHKD.model.*;
import com.example.FoodHKD.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TableService {

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TableDetailRepository tableDetailRepository;

    public Optional<Order> getOrderById(Integer orderId) {
        return orderRepository.findById(orderId);
    }


    public List<TableEntity> getAllTables() {
        return tableRepository.findAll();
    }

    public Optional<TableEntity> getTableById(Integer id) {
        return tableRepository.findById(id);
    }

    public TableEntity saveTable(TableEntity table) {
        return tableRepository.save(table);
    }

    public void deleteTable(Integer id) {
        tableRepository.deleteById(id);
    }

    public List<TableEntity> getAvailableTables() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<User> employeeOptional = userRepository.findByUsername(currentUsername);

        if (employeeOptional.isPresent()) {
            User currentEmployee = employeeOptional.get();
            return tableRepository.findByStatusOrEmployee("Trong", currentEmployee);
        }
        return tableRepository.findByStatus("Trong");
    }

    // TableDetail operations
    public List<TableDetail> getTableDetailsByTable(TableEntity table) {
        return tableDetailRepository.findByTable(table);
    }

    public TableDetail saveTableDetail(TableDetail tableDetail) {
        return tableDetailRepository.save(tableDetail);
    }

    public Optional<TableDetail> getTableDetailById(Integer id) {
        return tableDetailRepository.findById(id);
    }

    public void deleteTableDetail(Integer id) {
        tableDetailRepository.deleteById(id);
    }

    public void updateTableStatus(TableEntity table, String status) {
        table.setStatus(status);
        tableRepository.save(table);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;


    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public OrderDetail saveOrderDetail(OrderDetail orderDetail) {
        return orderDetailRepository.save(orderDetail);
    }

    public void deleteAllTableDetailsByTable(TableEntity table) {
        List<TableDetail> details = tableDetailRepository.findByTable(table);
        tableDetailRepository.deleteAll(details);
    }


}