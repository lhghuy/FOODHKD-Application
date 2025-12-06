package com.example.FoodHKD.controller.employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.FoodItem;
import com.example.FoodHKD.model.Order;
import com.example.FoodHKD.model.OrderDetail;
import com.example.FoodHKD.model.TableDetail;
import com.example.FoodHKD.model.TableEntity;
import com.example.FoodHKD.model.User;
import com.example.FoodHKD.repository.TableRepository;
import com.example.FoodHKD.repository.UserRepository;
import com.example.FoodHKD.service.FoodItemService;
import com.example.FoodHKD.service.TableService;

@RestController
@RequestMapping("/employee/tables")
public class TableEmployeeController {

    @Autowired
    private TableService tableService;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodItemService foodItemService;

    @GetMapping
    public String viewTables(Model model) {
        return "tables";
    }

    @GetMapping("/{tableId}")
    public String viewTableDetails(@PathVariable Integer tableId, Model model) {
        Optional<TableEntity> tableOptional = tableService.getTableById(tableId);
        if (tableOptional.isPresent()) {
            TableEntity table = tableOptional.get();
            List<TableDetail> tableDetails = tableService.getTableDetailsByTable(table);
            BigDecimal totalPrice = tableDetails.stream()
                    .map(TableDetail::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("table", table);
            model.addAttribute("tableDetails", tableDetails);
            model.addAttribute("totalPrice", totalPrice);
            return "table_detail";
        } else {
            return "redirect:/employee/tables";
        }
    }

    @GetMapping("/{tableId}/add-item-form")
    public String getAddItemForm(@PathVariable Integer tableId, Model model) {
        Optional<TableEntity> tableOptional = tableService.getTableById(tableId);
        List<FoodItem> allFoodItems = foodItemService.getAvailableFoodItems();
        if (tableOptional.isPresent()) {
            model.addAttribute("table", tableOptional.get());
            model.addAttribute("allFoodItems", allFoodItems);
            model.addAttribute("newTableDetail", new TableDetail());
            return "add_item_form";
        }
        return "error";
    }

    @PostMapping("/{tableId}/add")
    public ResponseEntity<String> addTableDetail(@PathVariable Integer tableId,
            @ModelAttribute TableDetail newTableDetail) {
        Optional<TableEntity> tableOptional = tableService.getTableById(tableId);
        FoodItem foodItem = foodItemService.getFoodItemById(newTableDetail.getFoodItem().getFoodID());

        if (tableOptional.isEmpty() || foodItem == null || newTableDetail.getQuantity() == null
                || newTableDetail.getQuantity() <= 0) {
            return new ResponseEntity<>("Invalid input for adding item.", HttpStatus.BAD_REQUEST);
        }

        if (foodItem.getQuantity() < newTableDetail.getQuantity()) {
            return new ResponseEntity<>("Số lượng sản phẩm " + foodItem.getName() + " không đủ. Chỉ còn "
                    + foodItem.getQuantity() + " sản phẩm.", HttpStatus.BAD_REQUEST);
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

    @GetMapping("/detail/{detailId}/edit-form")
    public String getEditItemForm(@PathVariable Integer detailId, Model model) {
        Optional<TableDetail> tableDetailOptional = tableService.getTableDetailById(detailId);
        if (tableDetailOptional.isPresent()) {
            model.addAttribute("tableDetail", tableDetailOptional.get());
            return "edit_item_form";
        }
        return "error";
    }

    @PostMapping("/detail/{detailId}/update")
    public ResponseEntity<String> updateTableDetail(@PathVariable Integer detailId,
            @RequestParam("quantity") Integer newQuantity) {
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
                return new ResponseEntity<>("Số lượng sản phẩm " + foodItem.getName() + " không đủ để tăng. Chỉ còn "
                        + foodItem.getQuantity() + " sản phẩm.", HttpStatus.BAD_REQUEST);
            }
        }

        foodItem.setQuantity(foodItem.getQuantity() - quantityDifference);
        foodItemService.saveFoodItem(foodItem);

        tableDetail.setQuantity(newQuantity);
        tableDetail.setTotalPrice(foodItem.getPrice().multiply(BigDecimal.valueOf(newQuantity)));
        tableService.saveTableDetail(tableDetail);

        return new ResponseEntity<>("Item updated successfully", HttpStatus.OK);
    }

    @DeleteMapping("/detail/{detailId}/delete")
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
    public String assignTable(@PathVariable Integer tableId) {
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
            return "redirect:/employee/tables/" + tableId;
        }
        return "redirect:/employee/tables";
    }

    @PostMapping("/{tableId}/checkout")
    public String checkoutTable(@PathVariable Integer tableId,
            @RequestParam String fullName,
            @RequestParam String phone) {
        Optional<TableEntity> tableOptional = tableService.getTableById(tableId);
        if (tableOptional.isEmpty()) {
            return "redirect:/employee/tables";
        }

        TableEntity table = tableOptional.get();
        List<TableDetail> tableDetails = tableService.getTableDetailsByTable(table);

        if (tableDetails.isEmpty()) {
            return "redirect:/employee/tables/" + tableId;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            return "redirect:/employee/tables";
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
            orderDetail.setPriceAtOrderTime(
                    tableDetail.getTotalPrice().divide(BigDecimal.valueOf(tableDetail.getQuantity()))); // đơn giá
            tableService.saveOrderDetail(orderDetail);
        }

        tableService.deleteAllTableDetailsByTable(table);

        table.setStatus("Trong");
        table.setEmployee(null);
        tableRepository.save(table);

        return "redirect:/employee/tables/order/" + savedOrder.getOrderID() + "/bill";

    }

    @GetMapping("/order/{orderId}/bill")
    public String showBill(@PathVariable Integer orderId, Model model) {
        Optional<Order> orderOptional = tableService.getOrderById(orderId);
        if (orderOptional.isEmpty()) {
            return "redirect:/employee/tables";
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

        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("total", total);
        return "order_bill";
    }

    // REST API endpoint for bill data
    @GetMapping("/api/order/{orderId}/bill")
    @ResponseBody
    public ResponseEntity<?> getBillData(@PathVariable Integer orderId) {
        try {
            Optional<Order> orderOptional = tableService.getOrderById(orderId);
            if (orderOptional.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Không tìm thấy hóa đơn"));
            }

            Order order = orderOptional.get();
            order.getTable().setOrders(null);
            order.setTableForView(order.getTable());
            List<OrderDetail> orderDetails = order.getOrderDetails();

            BigDecimal total = BigDecimal.ZERO;
            for (OrderDetail item : orderDetails) {
                if (item.getPriceAtOrderTime() != null && item.getQuantity() != null) {
                    BigDecimal itemTotal = item.getPriceAtOrderTime().multiply(BigDecimal.valueOf(item.getQuantity()));
                    total = total.add(itemTotal);
                }
            }

            Map<String, Object> billData = Map.of(
                    "order", order,
                    "orderDetails", orderDetails,
                    "total", total);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Lấy dữ liệu hóa đơn thành công",
                    "data", billData));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Lỗi hệ thống: " + e.getMessage()));
        }
    }

}