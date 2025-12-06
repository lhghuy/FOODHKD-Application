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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.FoodHKD.model.OnlineOrder;
import com.example.FoodHKD.model.OnlineOrderItem;
import com.example.FoodHKD.model.FoodItem;
import com.example.FoodHKD.model.User;
import com.example.FoodHKD.model.Coupon;
import com.example.FoodHKD.repository.OnlineOrderRepository;
import com.example.FoodHKD.repository.OnlineOrderItemRepository;
import com.example.FoodHKD.repository.FoodItemRepository;
import com.example.FoodHKD.repository.UserRepository;
import com.example.FoodHKD.repository.CouponRepository;
import com.example.FoodHKD.websocket.OrderWebSocketService;

@RestController
@RequestMapping("/api/online-orders")
public class OnlineOrderRest {

    @Autowired
    private OnlineOrderRepository onlineOrderRepository;

    @Autowired
    private OnlineOrderItemRepository onlineOrderItemRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private OrderWebSocketService orderWebSocketService;

    // ===== Helper Methods =====

    /**
     * Get currently authenticated user from SecurityContextHolder
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        
        String username = auth.getName();
        if (username != null && !username.isEmpty()) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            return userOpt.orElse(null);
        }
        
        return null;
    }

    /**
     * Check if current user has admin role (QuanLy)
     */
    private boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && "QuanLy".equalsIgnoreCase(user.getRole());
    }

    /**
     * Get customer ID from current user
     */
    private Integer getCurrentCustomerId() {
        User user = getCurrentUser();
        return user != null ? user.getUserID() : null;
    }

    // ===== Main Endpoints =====

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
            Integer customerId = (Integer) request.get("customerId");
            String customerName = (String) request.get("customerName");
            String phone = (String) request.get("phone");
            List<Map<String, Object>> itemsList = (List<Map<String, Object>>) request.get("items");

            if (customerId == null || customerName == null || itemsList == null || itemsList.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Missing required fields"));
            }

            OnlineOrder order = new OnlineOrder();
            order.setCustomerId(customerId);
            order.setCustomerName(customerName != null ? customerName : "Guest");
            order.setPhone(phone != null ? phone : "");
            order.setStatus("Pending");
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            BigDecimal total = BigDecimal.ZERO;
            List<OnlineOrderItem> items = new ArrayList<>();

            for (Map<String, Object> itemMap : itemsList) {
                Integer foodId = ((Number) itemMap.get("foodId")).intValue();
                String foodName = (String) itemMap.get("foodName");
                Integer quantity = ((Number) itemMap.get("quantity")).intValue();
                BigDecimal price = new BigDecimal(itemMap.get("priceAtOrderTime").toString());
                String imageUrl = (itemMap.get("imageUrl") != null) ? (String) itemMap.get("imageUrl") : "";

                OnlineOrderItem item = new OnlineOrderItem(foodId, foodName, quantity, price, imageUrl);
                item.setOnlineOrder(order);
                items.add(item);

                total = total.add(price.multiply(BigDecimal.valueOf(quantity)));
            }

            order.setTotal(total);
            order.setItems(items);

            OnlineOrder savedOrder = onlineOrderRepository.save(order);

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

    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            // 🔐 Authorization check: Only admin can see all orders
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Không có quyền truy cập. Vui lòng đăng nhập"));
            }

            List<OnlineOrder> orders;

            // If admin (QuanLy) -> get all orders
            if ("QuanLy".equalsIgnoreCase(currentUser.getRole())) {
                orders = onlineOrderRepository.findAll()
                    .stream()
                    .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                    .toList();
            } 
            // If not admin -> get only their own orders
            else {
                orders = onlineOrderRepository.findByCustomerId(currentUser.getUserID())
                    .stream()
                    .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                    .toList();
            }
            
            // Pre-fetch all food IDs from order items
            java.util.Set<Integer> foodIds = new java.util.HashSet<>();
            for (OnlineOrder order : orders) {
                if (order.getItems() != null) {
                    for (OnlineOrderItem item : order.getItems()) {
                        if (item.getImageUrl() == null || item.getImageUrl().isEmpty()) {
                            foodIds.add(item.getFoodId());
                        }
                    }
                }
            }
            
            // Fetch all food items at once (1 query instead of N queries)
            java.util.Map<Integer, FoodItem> foodCache = new java.util.HashMap<>();
            if (!foodIds.isEmpty()) {
                List<FoodItem> foods = foodItemRepository.findAllById(foodIds);
                for (FoodItem food : foods) {
                    foodCache.put(food.getFoodID(), food);
                }
            }
            
            List<Map<String, Object>> ordersList = new ArrayList<>();
            for (OnlineOrder order : orders) {
                ordersList.add(convertOrderToMap(order, foodCache));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Danh sách đơn hàng");
            response.put("orders", ordersList);
            response.put("total", ordersList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<?> getOrdersByCustomer(@PathVariable Integer customerId) {
        try {
            // 🔐 Authorization check: User can only see their own orders
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Không có quyền truy cập. Vui lòng đăng nhập"));
            }

            // If not admin AND not requesting own orders -> FORBIDDEN
            if (!"QuanLy".equalsIgnoreCase(currentUser.getRole()) && 
                !currentUser.getUserID().equals(customerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Bạn chỉ có thể xem đơn hàng của chính mình"));
            }

            List<OnlineOrder> orders = onlineOrderRepository.findByCustomerId(customerId)
                .stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .toList();
                
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

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer orderId) {
        try {
            // 🔐 Authorization check
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Không có quyền truy cập. Vui lòng đăng nhập"));
            }

            Optional<OnlineOrder> orderOpt = onlineOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Đơn hàng không tìm thấy"));
            }

            OnlineOrder order = orderOpt.get();

            // If not admin AND not owner of this order -> FORBIDDEN
            if (!"QuanLy".equalsIgnoreCase(currentUser.getRole()) && 
                !order.getCustomerId().equals(currentUser.getUserID())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Bạn chỉ có thể xem đơn hàng của chính mình"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chi tiết đơn hàng");
            response.put("order", convertOrderToMap(order));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<?> getOrderItems(@PathVariable Integer orderId) {
        try {
            List<OnlineOrderItem> items = onlineOrderItemRepository.findByOnlineOrderOrderId(orderId);
            List<Map<String, Object>> itemsList = new ArrayList<>();
            for (OnlineOrderItem item : items) {
                itemsList.add(convertItemToMap(item));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Danh sách items trong đơn");
            response.put("items", itemsList);
            response.put("total", itemsList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<?> addItemToOrder(
            @PathVariable Integer orderId,
            @RequestBody Map<String, Object> itemRequest) {
        try {
            Optional<OnlineOrder> orderOpt = onlineOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Đơn hàng không tìm thấy"));
            }

            OnlineOrder order = orderOpt.get();
            Integer foodId = ((Number) itemRequest.get("foodId")).intValue();
            String foodName = (String) itemRequest.get("foodName");
            Integer quantity = ((Number) itemRequest.get("quantity")).intValue();
            BigDecimal price = new BigDecimal(itemRequest.get("priceAtOrderTime").toString());
            String imageUrl = (itemRequest.get("imageUrl") != null) ? (String) itemRequest.get("imageUrl") : "";

            OnlineOrderItem item = new OnlineOrderItem(foodId, foodName, quantity, price, imageUrl);
            item.setOnlineOrder(order);

            OnlineOrderItem savedItem = onlineOrderItemRepository.save(item);

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
            order.setTotal(order.getTotal().add(itemTotal));
            order.setUpdatedAt(LocalDateTime.now());
            onlineOrderRepository.save(order);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Item được thêm vào đơn");
            response.put("item", convertItemToMap(savedItem));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestBody Map<String, Object> request) {
        try {
            Optional<OnlineOrder> orderOpt = onlineOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Đơn hàng không tìm thấy"));
            }

            String newStatus = (String) request.get("status");
            if (newStatus == null || newStatus.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Status không được để trống"));
            }

            OnlineOrder order = orderOpt.get();
            String reason = (String) request.getOrDefault("reason", "");

            if ("Cancelled".equalsIgnoreCase(newStatus)) {
                if (reason == null || reason.trim().isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Reason là bắt buộc khi hủy đơn hàng"));
                }
                order.setCancelReason(reason);
            }

            order.setStatus(newStatus);
            order.setUpdatedAt(LocalDateTime.now());
            OnlineOrder updatedOrder = onlineOrderRepository.save(order);

            Map<String, Object> orderMap = convertOrderToMap(updatedOrder);
            orderWebSocketService.notifyOrderStatusChanged(updatedOrder.getOrderId(), newStatus, orderMap);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Trạng thái đơn hàng được cập nhật");
            response.put("status", newStatus);
            if (!reason.isEmpty()) response.put("reason", reason);
            response.put("order", orderMap);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    private Map<String, Object> convertOrderToMap(OnlineOrder order) {
        return convertOrderToMap(order, new java.util.HashMap<>());
    }
    
    private Map<String, Object> convertOrderToMap(OnlineOrder order, java.util.Map<Integer, FoodItem> foodCache) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", order.getOrderId());
        map.put("customerId", order.getCustomerId());
        map.put("customerName", order.getCustomerName());
        map.put("phone", order.getPhone());
        map.put("notes", order.getNotes() != null ? order.getNotes() : "");
        map.put("total", order.getTotal());
        map.put("status", order.getStatus());
        map.put("discountAmount", order.getDiscountAmount());
        if (order.getAppliedCoupon() != null) {
            map.put("couponCode", order.getAppliedCoupon().getCode());
            map.put("discountPercent", order.getAppliedCoupon().getDiscountPercent());
        }
        if (order.getCancelReason() != null) {
            map.put("cancelReason", order.getCancelReason());
            map.put("reason", order.getCancelReason());
        }
        map.put("createdAt", order.getCreatedAt());
        map.put("updatedAt", order.getUpdatedAt());

        List<Map<String, Object>> itemsList = new ArrayList<>();
        if (order.getItems() != null) {
            for (OnlineOrderItem item : order.getItems()) {
                itemsList.add(convertItemToMap(item, foodCache));
            }
        }
        map.put("items", itemsList);
        return map;
    }

    private Map<String, Object> convertItemToMap(OnlineOrderItem item) {
        return convertItemToMap(item, new java.util.HashMap<>());
    }
    
    private Map<String, Object> convertItemToMap(OnlineOrderItem item, java.util.Map<Integer, FoodItem> foodCache) {
        Map<String, Object> map = new HashMap<>();
        map.put("itemId", item.getItemId());
        map.put("foodId", item.getFoodId());
        map.put("foodName", item.getFoodName());
        map.put("quantity", item.getQuantity());
        map.put("priceAtOrderTime", item.getPriceAtOrderTime());
        
        // Get image URL - from item or from cached FoodItem
        String imageUrl = item.getImageUrl();
        if ((imageUrl == null || imageUrl.isEmpty()) && !foodCache.isEmpty() && foodCache.containsKey(item.getFoodId())) {
            FoodItem food = foodCache.get(item.getFoodId());
            if (food != null && food.getAnh() != null) {
                imageUrl = food.getAnh();
            }
        }
        
        map.put("imageUrl", imageUrl != null ? imageUrl : "");
        map.put("status", item.getStatus());
        if (item.getCancelReason() != null) {
            map.put("cancelReason", item.getCancelReason());
        }
        return map;
    }

    /**
     * POST /api/online-orders/{orderId}/apply-coupon - Áp dụng mã giảm giá
     * Body: { "code": "SALE20" }
     */
    @PostMapping("/{orderId}/apply-coupon")
    public ResponseEntity<?> applyCoupon(
            @PathVariable Integer orderId,
            @RequestBody Map<String, Object> request) {
        try {
            Optional<OnlineOrder> orderOpt = onlineOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Đơn hàng không tìm thấy"));
            }

            String couponCode = (String) request.get("code");
            if (couponCode == null || couponCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mã giảm giá không được để trống"));
            }

            Optional<Coupon> couponOpt = couponRepository.findByCode(couponCode.toUpperCase());
            if (!couponOpt.isPresent()) {
                return ResponseEntity.ok()
                    .body(Map.of("success", false, "message", "Mã giảm không tồn tại"));
            }

            Coupon coupon = couponOpt.get();

            // Kiểm tra coupon còn hiệu lực
            if (!coupon.getIsActive()) {
                return ResponseEntity.ok()
                    .body(Map.of("success", false, "message", "Mã này đã hết hạn"));
            }

            // Kiểm tra hạn sử dụng
            if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
                return ResponseEntity.ok()
                    .body(Map.of("success", false, "message", "Mã này đã hết hạn"));
            }

            // Kiểm tra số lần sử dụng
            if (coupon.getUsageLimit() > 0 && coupon.getUsageCount() >= coupon.getUsageLimit()) {
                return ResponseEntity.ok()
                    .body(Map.of("success", false, "message", "Mã này đã hết hạn"));
            }

            OnlineOrder order = orderOpt.get();
            BigDecimal discountAmount = order.getTotal()
                .multiply(BigDecimal.valueOf(coupon.getDiscountPercent()))
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

            order.setAppliedCoupon(coupon);
            order.setDiscountAmount(discountAmount);
            order.setUpdatedAt(LocalDateTime.now());

            // Tăng usage count
            coupon.setUsageCount(coupon.getUsageCount() + 1);
            couponRepository.save(coupon);

            OnlineOrder updatedOrder = onlineOrderRepository.save(order);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Áp dụng mã giảm giá thành công");
            response.put("discountPercent", coupon.getDiscountPercent());
            response.put("discountAmount", discountAmount);
            response.put("totalAfterDiscount", order.getTotal().subtract(discountAmount));
            response.put("order", convertOrderToMap(updatedOrder));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }
}
