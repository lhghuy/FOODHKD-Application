package com.example.FoodHKD.controller.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.service.CouponService;

/**
 * REST Controller for Client Coupon Validation
 * Endpoints: /api/coupons
 * Public access - No authentication required for validation
 */
@RestController
@RequestMapping("/api/coupons")
public class CouponClientController {

    @Autowired
    private CouponService couponService;

    /**
     * POST /api/coupons/validate - Validate coupon code
     * 
     * Request Body:
     * {
     *   "code": "SALE20"
     * }
     * 
     * Response (Valid):
     * {
     *   "valid": true,
     *   "message": "Mã giảm giá hợp lệ",
     *   "discountPercent": 20,
     *   "couponId": 1,
     *   "code": "SALE20",
     *   "expiryDate": "2025-12-31T23:59:59"
     * }
     * 
     * Response (Invalid):
     * {
     *   "valid": false,
     *   "message": "Mã giảm giá không tồn tại",
     *   "discountPercent": 0
     * }
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            Map<String, Object> result = couponService.validateCoupon(code);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "valid", false,
                            "message", "Lỗi server: " + e.getMessage(),
                            "discountPercent", 0
                    ));
        }
    }
}
