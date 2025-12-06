package com.example.FoodHKD.rest.employee;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.repository.TableRepository;
import com.example.FoodHKD.repository.UserRepository;
import com.example.FoodHKD.service.FoodItemService;
import com.example.FoodHKD.service.TableService;
import com.example.FoodHKD.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee/tables")
public class TableEmployeeRest {
    @Autowired
    private TableService tableService;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodItemService foodItemService;

    @GetMapping
    public ResponseEntity<List<TableEntity>> getTables() {
        List<TableEntity> availableTables = tableService.getAvailableTables().stream().peek(table -> {
            if(table.getEmployee() != null) {
                table.getEmployee().setTableEntities(null);
            }
        }).collect(Collectors.toList());
        return ResponseEntity.ok(availableTables);
    }

    @GetMapping("/{tableId}")
    public ResponseEntity<Map<String, Object>> getTableDetails(@PathVariable Integer tableId) {
        Optional<TableEntity> tableOptional = tableService.getTableById(tableId);
        if (tableOptional.isPresent()) {
            TableEntity table = tableOptional.get();
            List<TableDetail> tableDetails = tableService.getTableDetailsByTable(table);
            BigDecimal totalPrice = tableDetails.stream()
                    .map(TableDetail::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> response = new HashMap<>();
            if (table.getEmployee() != null) {
                table.getEmployee().setTableEntities(null);
            }
            
            response.put("table", table);
            response.put("tableDetails", tableDetails);
            response.put("totalPrice", totalPrice);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{tableId}/food-items")
    public ResponseEntity<Map<String, Object>> getAvailableFoodItems(@PathVariable Integer tableId) {
        Optional<TableEntity> tableOptional = tableService.getTableById(tableId);
        if (tableOptional.isPresent()) {
            List<FoodItem> allFoodItems = foodItemService.getAvailableFoodItems();
            
            Map<String, Object> response = new HashMap<>();
            tableOptional.ifPresent(table -> {
                if (table.getEmployee() != null) {
                    table.getEmployee().setTableEntities(null);
                }
                response.put("table", table);
                response.put("foodItems", allFoodItems.stream().peek(foodItem -> {
                    if(foodItem.getCategory() != null) {
                        foodItem.getCategory().setFoodItems(null);
                    }
                }).collect(Collectors.toList()));
            });
            
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(value  = "/{tableId}/add")
    public ResponseEntity<String> addTableDetail(@PathVariable Integer tableId, @RequestBody TableDetail newTableDetail) {
        Optional<TableEntity> tableOptional = tableService.getTableById(tableId);
        FoodItem foodItem = foodItemService.getFoodItemById(newTableDetail.getFoodItem().getFoodID());

        if (tableOptional.isEmpty() || foodItem == null || newTableDetail.getQuantity() == null || newTableDetail.getQuantity() <= 0) {
            return new ResponseEntity<>("Invalid input for adding item.", HttpStatus.BAD_REQUEST);
        }

        if (foodItem.getQuantity() < newTableDetail.getQuantity()) {
            return new ResponseEntity<>("Số lượng sản phẩm " + foodItem.getName() + " không đủ. Chỉ còn " + foodItem.getQuantity() + " sản phẩm.", HttpStatus.BAD_REQUEST);
        }

        TableEntity table = tableOptional.get();

        foodItem.setQuantity(foodItem.getQuantity() - newTableDetail.getQuantity());
        foodItemService.saveFoodItem(foodItem);

        TableDetail detail = new TableDetail();
        detail.setTable(table);
        detail.setFoodItem(foodItem);
        detail.setQuantity(newTableDetail.getQuantity());
        detail.setTotalPrice(foodItem.getPrice().multiply(BigDecimal.valueOf(newTableDetail.getQuantity())));

        tableService.saveTableDetail(detail);
        return new ResponseEntity<>("Item added successfully", HttpStatus.OK);
    }

    @GetMapping("/detail/{detailId}")
    public ResponseEntity<TableDetail> getTableDetail(@PathVariable Integer detailId) {
        Optional<TableDetail> tableDetailOptional = tableService.getTableDetailById(detailId);
        if (tableDetailOptional.isPresent()) {
            return ResponseEntity.ok(tableDetailOptional.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/detail/{detailId}")
    public ResponseEntity<String> updateTableDetail(@PathVariable Integer detailId, @RequestBody Map<String, Integer> request) {
        Integer newQuantity = request.get("quantity");
        Optional<TableDetail> tableDetailOptional = tableService.getTableDetailById(detailId);

        if (tableDetailOptional.isEmpty() || newQuantity == null || newQuantity <= 0) {
            return new ResponseEntity<>("Invalid input for updating item.", HttpStatus.BAD_REQUEST);
        }

        TableDetail tableDetail = tableDetailOptional.get();
        FoodItem foodItem = tableDetail.getFoodItem();
        Integer oldQuantity = tableDetail.getQuantity();

        int quantityDifference = newQuantity - oldQuantity;

        if (quantityDifference > 0) {
            if (foodItem.getQuantity() < quantityDifference) {
                return new ResponseEntity<>("Số lượng sản phẩm " + foodItem.getName() + " không đủ để tăng. Chỉ còn " + foodItem.getQuantity() + " sản phẩm.", HttpStatus.BAD_REQUEST);
            }
        }

        foodItem.setQuantity(foodItem.getQuantity() - quantityDifference);
        foodItemService.saveFoodItem(foodItem);

        tableDetail.setQuantity(newQuantity);
        tableDetail.setTotalPrice(foodItem.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
        tableService.saveTableDetail(tableDetail);

        return new ResponseEntity<>("Item updated successfully", HttpStatus.OK);
    }

    @DeleteMapping("/detail/{detailId}")
    public ResponseEntity<String> deleteTableDetail(@PathVariable Integer detailId) {
        Optional<TableDetail> tableDetailOptional = tableService.getTableDetailById(detailId);
        if (tableDetailOptional.isEmpty()) {
            return new ResponseEntity<>("Item not found", HttpStatus.NOT_FOUND);
        }

        TableDetail tableDetail = tableDetailOptional.get();
        FoodItem foodItem = tableDetail.getFoodItem();

        foodItem.setQuantity(foodItem.getQuantity() + tableDetail.getQuantity());
        foodItemService.saveFoodItem(foodItem);

        tableService.deleteTableDetail(detailId);
        return new ResponseEntity<>("Item deleted successfully", HttpStatus.OK);
    }

    @PostMapping("/{tableId}/assign")
    public ResponseEntity<String> assignTable(@PathVariable Integer tableId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Optional<TableEntity> tableOptional = tableRepository.findById(tableId);
        Optional<User> employeeOptional = userRepository.findByUsername(currentUsername);

        if (tableOptional.isPresent() && employeeOptional.isPresent()) {
            TableEntity table = tableOptional.get();
            User currentEmployee = employeeOptional.get();
            table.setEmployee(currentEmployee);
            table.setStatus("DangPhucVu");
            tableRepository.save(table);
            return ResponseEntity.ok("Table assigned successfully");
        }
        return ResponseEntity.badRequest().body("Failed to assign table");
    }

    @PostMapping("/{tableId}/checkout")
    public ResponseEntity<Map<String, Object>> checkoutTable(@PathVariable Integer tableId,
                                                             @RequestBody Map<String, String> checkoutRequest) {
        String fullName = checkoutRequest.get("fullName");
        String phone = checkoutRequest.get("phone");
        
        Optional<TableEntity> tableOptional = tableService.getTableById(tableId);
        if (tableOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        TableEntity table = tableOptional.get();
        List<TableDetail> tableDetails = tableService.getTableDetailsByTable(table);

        if (tableDetails.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No items in table to checkout"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        User currentUser = userOptional.get();

        Order order = new Order();
        order.setTable(table);
        order.setCreatedBy(currentUser);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("Đã thanh toán");
        order.setFullName(fullName);
        order.setPhone(phone);
        Order savedOrder = tableService.saveOrder(order);

        for (TableDetail tableDetail : tableDetails) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(savedOrder);
            orderDetail.setFoodItem(tableDetail.getFoodItem());
            orderDetail.setQuantity(tableDetail.getQuantity());
            orderDetail.setPriceAtOrderTime(tableDetail.getTotalPrice().divide(BigDecimal.valueOf(tableDetail.getQuantity()))); // đơn giá
            tableService.saveOrderDetail(orderDetail);
        }

        tableService.deleteAllTableDetailsByTable(table);

        table.setStatus("Trong");
        table.setEmployee(null);
        tableRepository.save(table);

        Map<String, Object> response = new HashMap<>();
        response.put("orderId", savedOrder.getOrderID());
        response.put("message", "Checkout successful");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/order/{orderId}/bill")
    public ResponseEntity<Map<String, Object>> getBill(@PathVariable Integer orderId) {
        Optional<Order> orderOptional = tableService.getOrderById(orderId);
        if (orderOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Order order = orderOptional.get();
        List<OrderDetail> orderDetails = order.getOrderDetails();

        BigDecimal total = BigDecimal.ZERO;
        for (OrderDetail item : orderDetails) {
            if (item.getPriceAtOrderTime() != null && item.getQuantity() != null) {
                BigDecimal itemTotal = item.getPriceAtOrderTime().multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("order", order);
        response.put("orderDetails", orderDetails);
        response.put("total", total);
        
        return ResponseEntity.ok(response);
    }
}
