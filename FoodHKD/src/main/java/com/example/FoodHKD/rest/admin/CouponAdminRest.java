package com.example.FoodHKD.rest.admin;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.Coupon;
import com.example.FoodHKD.model.User;
import com.example.FoodHKD.repository.CouponRepository;
import com.example.FoodHKD.repository.UserRepository;

/**
 * REST Controller for Admin Coupon Management
 * Endpoints: /api/admin/coupons
 * Authorization: Only Admin (QuanLy) can access
 */
@RestController
@RequestMapping("/api/admin/coupons")
public class CouponAdminRest {

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get current authenticated user from SecurityContext
     * FIXED: Query database to get User object instead of using principal
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
     * GET /api/admin/coupons - Lấy tất cả mã giảm giá
     */
    @GetMapping
    public ResponseEntity<?> getAllCoupons() {
        // Authorization check
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "Chỉ admin mới có thể truy cập"));
        }

        try {
            List<Coupon> coupons = couponRepository.findAll();
            List<Map<String, Object>> couponsList = new ArrayList<>();

            for (Coupon coupon : coupons) {
                couponsList.add(convertCouponToMap(coupon));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("coupons", couponsList);
            response.put("total", couponsList.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * POST /api/admin/coupons - Tạo mã giảm giá mới
     * Body: {
     *   "code": "SALE20",
     *   "discountPercent": 20,
     *   "expiryDate": "2025-12-31T23:59:59",
     *   "usageLimit": 100
     * }
     */
    @PostMapping
    public ResponseEntity<?> createCoupon(@RequestBody Map<String, Object> request) {
        // Authorization check
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "Chỉ admin mới có thể tạo mã giảm giá"));
        }

        try {
            String code = (String) request.get("code");
            Integer discountPercent = ((Number) request.get("discountPercent")).intValue();
            String expiryDateStr = (String) request.get("expiryDate");
            Integer usageLimit = ((Number) request.get("usageLimit")).intValue();

            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mã giảm giá không được để trống"));
            }

            Optional<Coupon> existingCoupon = couponRepository.findByCode(code.toUpperCase());
            if (existingCoupon.isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Mã giảm giá đã tồn tại"));
            }

            Coupon coupon = new Coupon();
            coupon.setCode(code.toUpperCase());
            coupon.setDiscountPercent(discountPercent);
            coupon.setExpiryDate(LocalDateTime.parse(expiryDateStr));
            coupon.setUsageLimit(usageLimit);
            coupon.setIsActive(true);

            Coupon savedCoupon = couponRepository.save(coupon);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mã giảm giá được tạo thành công");
            response.put("coupon", convertCouponToMap(savedCoupon));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/admin/coupons/{couponId} - Cập nhật mã giảm giá
     */
    @PutMapping("/{couponId}")
    public ResponseEntity<?> updateCoupon(
            @PathVariable Integer couponId,
            @RequestBody Map<String, Object> request) {
        // Authorization check
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "Chỉ admin mới có thể cập nhật mã giảm giá"));
        }

        try {
            Optional<Coupon> couponOpt = couponRepository.findById(couponId);
            if (!couponOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Mã giảm giá không tìm thấy"));
            }

            Coupon coupon = couponOpt.get();
            
            if (request.containsKey("discountPercent")) {
                coupon.setDiscountPercent(((Number) request.get("discountPercent")).intValue());
            }
            if (request.containsKey("expiryDate")) {
                coupon.setExpiryDate(LocalDateTime.parse((String) request.get("expiryDate")));
            }
            if (request.containsKey("usageLimit")) {
                coupon.setUsageLimit(((Number) request.get("usageLimit")).intValue());
            }
            if (request.containsKey("isActive")) {
                coupon.setIsActive((Boolean) request.get("isActive"));
            }

            coupon.setUpdatedAt(LocalDateTime.now());
            Coupon updatedCoupon = couponRepository.save(coupon);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Mã giảm giá được cập nhật");
            response.put("coupon", convertCouponToMap(updatedCoupon));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/admin/coupons/{couponId} - Xóa mã giảm giá
     */
    @DeleteMapping("/{couponId}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Integer couponId) {
        // Authorization check
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("success", false, "message", "Chỉ admin mới có thể xóa mã giảm giá"));
        }

        try {
            if (!couponRepository.existsById(couponId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Mã giảm giá không tìm thấy"));
            }

            couponRepository.deleteById(couponId);

            return ResponseEntity.ok(Map.of("success", true, "message", "Mã giảm giá được xóa"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Error: " + e.getMessage()));
        }
    }

    private Map<String, Object> convertCouponToMap(Coupon coupon) {
        Map<String, Object> map = new HashMap<>();
        map.put("couponId", coupon.getCouponId());
        map.put("code", coupon.getCode());
        map.put("discountPercent", coupon.getDiscountPercent());
        map.put("expiryDate", coupon.getExpiryDate());
        map.put("usageLimit", coupon.getUsageLimit());
        map.put("usageCount", coupon.getUsageCount());
        map.put("isActive", coupon.getIsActive());
        map.put("createdAt", coupon.getCreatedAt());
        return map;
    }
}
