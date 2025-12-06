package com.example.FoodHKD.rest.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.Coupon;
import com.example.FoodHKD.model.OnlineOrder;
import com.example.FoodHKD.model.OnlineOrderItem;
import com.example.FoodHKD.repository.CouponRepository;
import com.example.FoodHKD.repository.OnlineOrderRepository;
import com.example.FoodHKD.repository.OnlineOrderItemRepository;
import com.example.FoodHKD.service.CouponService;
import com.example.FoodHKD.service.OnlineOrderService;
import com.example.FoodHKD.websocket.OrderWebSocketService;

/**
 * REST Controller for Online Order Management (E-commerce)
 * Endpoints: /api/orders (khác với /api/online-orders)
 * Lưu trữ đơn hàng online từ khách hàng (persistent storage)
 * 
 * NOTE: OnlineOrderRest.java xử lý /api/online-orders
 *       OnlineOrderController.java xử lý /api/orders
 */
@RestController
@RequestMapping("/api/orders")
public class OnlineOrderController {

    @Autowired
    private OnlineOrderRepository onlineOrderRepository;

    @Autowired
    private OnlineOrderItemRepository onlineOrderItemRepository;

    @Autowired
    private OnlineOrderService onlineOrderService;

    @Autowired
    private OrderWebSocketService orderWebSocketService;

    @Autowired
    private CouponService couponService;

    /**
     * POST /api/orders/calculate - Tính toán giá và áp dụng coupon (KHÔNG tạo đơn hàng)
     * 
     * Request:
     * {
     *   "items": [
     *     {
     *       "foodId": 1,
     *       "foodName": "Pizza",
     *       "quantity": 2,
     *       "priceAtOrderTime": 100000
     *     }
     *   ],
     *   "couponCode": "SALE20" (optional)
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "subtotal": 200000,
     *   "discountPercent": 20,
     *   "discountAmount": 40000,
     *   "total": 160000,
     *   "couponCode": "SALE20"
     * }
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateOrderTotal(@RequestBody Map<String, Object> request) {
        try {
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) request.get("items");
            String couponCode = (String) request.getOrDefault("couponCode", "");

            if (itemsList == null || itemsList.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Danh sách sản phẩm không được trống"));
            }

            // Calculate subtotal
            BigDecimal subtotal = BigDecimal.ZERO;
            for (Map<String, Object> itemMap : itemsList) {
                Integer quantity = ((Number) itemMap.get("quantity")).intValue();
                BigDecimal price = new BigDecimal(itemMap.get("priceAtOrderTime").toString());
                subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(quantity)));
            }

            // Calculate discount if coupon is provided
            BigDecimal discountAmount = BigDecimal.ZERO;
            Integer discountPercent = 0;

            if (couponCode != null && !couponCode.trim().isEmpty()) {
                Map<String, Object> couponValidation = couponService.validateCoupon(couponCode);
                
                if ((Boolean) couponValidation.get("valid")) {
                    discountPercent = (Integer) couponValidation.get("discountPercent");
                    discountAmount = subtotal.multiply(BigDecimal.valueOf(discountPercent))
                            .divide(BigDecimal.valueOf(100));
                } else {
                    return ResponseEntity.badRequest()
                        .body(Map.of(
                            "success", false,
                            "message", (String) couponValidation.get("message"),
                            "subtotal", subtotal,
                            "discountAmount", BigDecimal.ZERO,
                            "total", subtotal
                        ));
                }
            }

            BigDecimal total = subtotal.subtract(discountAmount);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Tính toán đơn hàng thành công");
            response.put("subtotal", subtotal);
            response.put("discountPercent", discountPercent);
            response.put("discountAmount", discountAmount);
            response.put("total", total);
            if (discountPercent > 0) {
                response.put("couponCode", couponCode);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/orders - Tạo đơn hàng mới (Client)
     */
    /**
     * POST /api/orders - Tạo đơn hàng mới (Client)
     * 
     * Request:
     * {
     *   "customerId": 1,
     *   "customerName": "John Doe",
     *   "phone": "0123456789",
     *   "items": [...],
     *   "couponCode": "SALE20" (optional),
     *   "discountAmount": 40000 (optional - from calculate endpoint)
     * }
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Integer customerId = (Integer) request.get("customerId");
            String customerName = (String) request.get("customerName");
            String phone = (String) request.get("phone");
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) request.get("items");
            String couponCode = (String) request.getOrDefault("couponCode", "");
            String notes = (String) request.getOrDefault("notes", "");

            if (customerId == null || customerName == null || itemsList == null || itemsList.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Missing required fields"));
            }

            // Create order
            OnlineOrder order = new OnlineOrder();
            order.setCustomerId(customerId);
            order.setCustomerName(customerName);
            order.setPhone(phone != null ? phone : "");
            order.setNotes(notes);
            order.setStatus("Pending");
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            // Calculate total and create items
            BigDecimal total = BigDecimal.ZERO;
            List<OnlineOrderItem> items = new ArrayList<>();

            // Create items
            for (Map<String, Object> itemMap : itemsList) {
                Integer foodId = ((Number) itemMap.get("foodId")).intValue();
                String foodName = (String) itemMap.get("foodName");
                Integer quantity = ((Number) itemMap.get("quantity")).intValue();
                BigDecimal price = new BigDecimal(itemMap.get("priceAtOrderTime").toString());
                String imageUrl = (itemMap.get("imageUrl") != null) ? (String) itemMap.get("imageUrl") : "";

                OnlineOrderItem item = new OnlineOrderItem(foodId, foodName, quantity, price, imageUrl);
                items.add(item);

                // Calculate total
                total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
            }

            // Apply coupon if provided
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (couponCode != null && !couponCode.trim().isEmpty()) {
                Map<String, Object> couponValidation = couponService.validateCoupon(couponCode);
                
                if ((Boolean) couponValidation.get("valid")) {
                    Integer discountPercent = (Integer) couponValidation.get("discountPercent");
                    Integer couponId = (Integer) couponValidation.get("couponId");
                    
                    // Get and set coupon
                    Coupon coupon = couponService.getCouponByCode(couponCode);
                    if (coupon != null) {
                        order.setAppliedCoupon(coupon);
                        discountAmount = total.multiply(BigDecimal.valueOf(discountPercent))
                                .divide(BigDecimal.valueOf(100));
                        order.setDiscountAmount(discountAmount);
                        
                        // Increment coupon usage
                        couponService.incrementUsageCount(couponId);
                    }
                }
            }

            // Final total after discount
            total = total.subtract(discountAmount);
            
            order.setTotal(total);
            order.setItems(items);

            // Set order reference for items
            for (OnlineOrderItem item : items) {
                item.setOnlineOrder(order);
            }

            // Save order
            OnlineOrder savedOrder = onlineOrderRepository.save(order);

            // Broadcast new order to all connected admin clients via WebSocket
            Map<String, Object> orderMap = convertOrderToMap(savedOrder);
            orderWebSocketService.notifyOrderCreated(orderMap);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đơn hàng được tạo thành công");
            response.put("orderId", savedOrder.getOrderId());
            response.put("order", orderMap);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/orders - Lấy tất cả đơn hàng (Client view)
     */
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            List<OnlineOrder> orders = onlineOrderRepository.findAll();
            List<Map<String, Object>> ordersList = new ArrayList<>();

            for (OnlineOrder order : orders) {
                ordersList.add(convertOrderToMap(order));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Danh sách tất cả đơn hàng");
            response.put("orders", ordersList);
            response.put("total", ordersList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/orders/{orderId} - Lấy chi tiết đơn hàng
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer orderId) {
        try {
            Optional<OnlineOrder> orderOpt = onlineOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Đơn hàng không tìm thấy"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chi tiết đơn hàng");
            response.put("order", convertOrderToMap(orderOpt.get()));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/orders/customer/{customerId} - Lấy đơn hàng của khách
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getOrdersByCustomer(@PathVariable Integer customerId) {
        try {
            List<OnlineOrder> orders = onlineOrderRepository.findByCustomerId(customerId);
            List<Map<String, Object>> ordersList = new ArrayList<>();

            for (OnlineOrder order : orders) {
                ordersList.add(convertOrderToMap(order));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Danh sách đơn hàng của khách hàng");
            response.put("orders", ordersList);
            response.put("total", ordersList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * GET /api/orders/status/{status} - Lọc đơn hàng theo trạng thái
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            List<OnlineOrder> orders = onlineOrderRepository.findByStatus(status);
            List<Map<String, Object>> ordersList = new ArrayList<>();

            for (OnlineOrder order : orders) {
                ordersList.add(convertOrderToMap(order));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đơn hàng theo trạng thái: " + status);
            response.put("orders", ordersList);
            response.put("total", ordersList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * Convert OnlineOrder to Map
     */
    private Map<String, Object> convertOrderToMap(OnlineOrder order) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("customerId", order.getCustomerId());
        map.put("customerName", order.getCustomerName());
        map.put("phone", order.getPhone());
        map.put("notes", order.getNotes() != null ? order.getNotes() : "");
        map.put("total", order.getTotal());
        map.put("status", order.getStatus());
        map.put("createdAt", order.getCreatedAt());
        map.put("updatedAt", order.getUpdatedAt());
        
        // Add coupon info
        if (order.getAppliedCoupon() != null) {
            map.put("couponCode", order.getAppliedCoupon().getCode());
            map.put("discountAmount", order.getDiscountAmount());
            map.put("discountPercent", order.getAppliedCoupon().getDiscountPercent());
        }
        
        List<Map<String, Object>> itemsList = new ArrayList<>();
        if (order.getItems() != null) {
            for (OnlineOrderItem item : order.getItems()) {
                itemsList.add(convertItemToMap(item));
            }
        }
        map.put("items", itemsList);

        return map;
    }

    /**
     * Convert OnlineOrderItem to Map
     */
    private Map<String, Object> convertItemToMap(OnlineOrderItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", item.getItemId());
        map.put("foodId", item.getFoodId());
        map.put("foodName", item.getFoodName());
        map.put("quantity", item.getQuantity());
        map.put("priceAtOrderTime", item.getPriceAtOrderTime());
        map.put("status", item.getStatus());
        map.put("cancelReason", item.getCancelReason());
        map.put("imageUrl", item.getImageUrl());
        return map;
    }

    /**
     * PUT /api/orders/{orderId}/items/{itemId}/cancel - Hủy một item trong đơn hàng
     */
    @PutMapping("/{orderId}/items/{itemId}/cancel")
    public ResponseEntity<?> cancelOrderItem(
            @PathVariable Integer orderId,
            @PathVariable Integer itemId,
            @RequestBody Map<String, Object> request) {
        try {
            String reason = (String) request.getOrDefault("reason", "");

            // Cancel item via service
            OnlineOrderItem cancelledItem = onlineOrderService.cancelOrderItem(orderId, itemId, reason);

            // Get updated order for WebSocket broadcast
            Optional<OnlineOrder> orderOpt = onlineOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Đơn hàng không tìm thấy"));
            }

            OnlineOrder order = orderOpt.get();
            Map<String, Object> orderMap = convertOrderToMap(order);
            Map<String, Object> itemMap = convertItemToMap(cancelledItem);

            // Broadcast WebSocket notification
            orderWebSocketService.notifyOrderItemCancelled(orderId, itemId, reason, itemMap);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Item được hủy thành công");
            response.put("item", itemMap);
            response.put("order", orderMap);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }
}
