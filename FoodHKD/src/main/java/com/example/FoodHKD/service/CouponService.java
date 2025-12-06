package com.example.FoodHKD.service;

import com.example.FoodHKD.model.Coupon;
import java.util.Map;

public interface CouponService {
    /**
     * Validate coupon code
     * @param code The coupon code to validate
     * @return Map with validation result and discount percentage
     */
    Map<String, Object> validateCoupon(String code);
    
    /**
     * Get coupon by code
     * @param code The coupon code
     * @return Coupon object or null if not found
     */
    Coupon getCouponByCode(String code);
    
    /**
     * Increment usage count of a coupon
     * @param couponId The coupon ID
     */
    void incrementUsageCount(Integer couponId);
}
