package com.example.FoodHKD.rest.admin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.Order;
import com.example.FoodHKD.model.OrderDetail;
import com.example.FoodHKD.repository.OrderDetailRepository;
import com.example.FoodHKD.repository.OrderRepository;
import com.example.FoodHKD.repository.TableRepository;
import com.example.FoodHKD.repository.UserRepository;
import com.example.FoodHKD.websocket.OrderWebSocketHandler;

@RestController
@RequestMapping("/api/admin/orders")
public class OrderAdminRest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
private OrderWebSocketHandler orderWebSocketHandler;


    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        try {
            List<Order> orders = orderRepository.findAll();

            List<Map<String, Object>> ordersWithTotals = new ArrayList<>();
            for (Order order : orders) {
                Map<String, Object> orderData = convertOrderToMap(order);
                orderData.put("total", calculateOrderTotal(order));
                ordersWithTotals.add(orderData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Danh sách hóa đơn được tải thành công");
            response.put("data", ordersWithTotals);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi tải danh sách hóa đơn: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Integer id) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(id);
            if (!orderOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn với ID: " + id);

                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            Map<String, Object> orderData = convertOrderToMap(order);
            orderData.put("total", calculateOrderTotal(order));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thông tin hóa đơn được tải thành công");
            response.put("data", orderData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi tải thông tin hóa đơn: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getOrderDetails(@PathVariable Integer id) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(id);
            if (!orderOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn với ID: " + id);

                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);

            Map<String, Object> result = new HashMap<>();
            result.put("order", convertOrderToMap(order));
            result.put("orderDetails", convertOrderDetailsToList(orderDetails));
            result.put("total", calculateOrderTotal(order));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chi tiết hóa đơn được tải thành công");
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi tải chi tiết hóa đơn: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> request) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(id);
            if (!orderOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn với ID: " + id);

                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();
            String newStatus = request.get("status");

            if (newStatus == null || newStatus.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trạng thái không được để trống");

                return ResponseEntity.badRequest().body(response);
            }

            // Validate status values
            List<String> validStatuses = Arrays.asList("Pending", "Preparing", "Ready", "Served", "Cancelled");
            if (!validStatuses.contains(newStatus)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trạng thái không hợp lệ: " + newStatus);

                return ResponseEntity.badRequest().body(response);
            }

            order.setStatus(newStatus);
            orderRepository.save(order);

            // Broadcast order status update via WebSocket
            Map<String, Object> orderData = convertOrderToMap(order);
            orderWebSocketHandler.broadcastOrderStatusUpdate(id, newStatus, orderData);


            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái hóa đơn thành công");
            response.put("data", orderData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi cập nhật trạng thái: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(@PathVariable String status) {
        try {
            List<Order> orders = orderRepository.findByStatus(status);

            List<Map<String, Object>> ordersWithTotals = new ArrayList<>();
            for (Order order : orders) {
                Map<String, Object> orderData = convertOrderToMap(order);
                orderData.put("total", calculateOrderTotal(order));
                ordersWithTotals.add(orderData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Danh sách hóa đơn theo trạng thái được tải thành công");
            response.put("data", ordersWithTotals);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi tải hóa đơn theo trạng thái: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/statuses")
    public ResponseEntity<Map<String, Object>> getAvailableStatuses() {
        try {
            List<String> statuses = Arrays.asList("Pending", "Preparing", "Ready", "Served", "Cancelled");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Danh sách trạng thái được tải thành công");
            response.put("data", statuses);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi tải danh sách trạng thái: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Integer id) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(id);
            if (!orderOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không tìm thấy hóa đơn với ID: " + id);

                return ResponseEntity.notFound().build();
            }

            Order order = orderOpt.get();

            // Check if order can be deleted (e.g., not served yet)
            if ("Served".equals(order.getStatus())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Không thể xóa hóa đơn đã được phục vụ");

                return ResponseEntity.badRequest().body(response);
            }

            // Delete order details first
            List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
            orderDetailRepository.deleteAll(orderDetails);

            // Then delete the order
            orderRepository.delete(order);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xóa hóa đơn thành công");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Lỗi khi xóa hóa đơn: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    private Map<String, Object> convertOrderToMap(Order order) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderID", order.getOrderID());
        orderData.put("createdAt", order.getCreatedAt());
        orderData.put("status", order.getStatus());
        orderData.put("fullName", order.getFullName());
        orderData.put("phone", order.getPhone());

        if (order.getTable() != null) {
            Map<String, Object> tableData = new HashMap<>();
            tableData.put("tableID", order.getTable().getTableID());
            tableData.put("tableNumber", order.getTable().getTableNumber());
            orderData.put("table", tableData);
        }

        if (order.getCreatedBy() != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("userID", order.getCreatedBy().getUserID());
            userData.put("username", order.getCreatedBy().getUsername());
            userData.put("fullName", order.getCreatedBy().getFullName());
            orderData.put("createdBy", userData);
        }

        return orderData;
    }

    private List<Map<String, Object>> convertOrderDetailsToList(List<OrderDetail> orderDetails) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (OrderDetail detail : orderDetails) {
            Map<String, Object> detailData = new HashMap<>();
            detailData.put("orderDetailID", detail.getOrderDetailID());
            detailData.put("quantity", detail.getQuantity());
            detailData.put("priceAtOrderTime", detail.getPriceAtOrderTime());

            if (detail.getFoodItem() != null) {
                Map<String, Object> foodData = new HashMap<>();
                foodData.put("foodID", detail.getFoodItem().getFoodID());
                foodData.put("name", detail.getFoodItem().getName());
                foodData.put("anh", detail.getFoodItem().getAnh());
                detailData.put("foodItem", foodData);
            }

            result.add(detailData);
        }
        return result;
    }

    private BigDecimal calculateOrderTotal(Order order) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        BigDecimal total = BigDecimal.ZERO;

        for (OrderDetail item : orderDetails) {
            if (item.getPriceAtOrderTime() != null && item.getQuantity() != null) {
                BigDecimal itemTotal = item.getPriceAtOrderTime().multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(itemTotal);
            }
        }

        return total;
    }
}
