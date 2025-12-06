package com.example.FoodHKD.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.FoodHKD.model.Coupon;
import com.example.FoodHKD.repository.CouponRepository;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponRepository couponRepository;

    @Override
    public Map<String, Object> validateCoupon(String code) {
        Map<String, Object> response = new HashMap<>();

        // Kiểm tra code không được trống
        if (code == null || code.trim().isEmpty()) {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không được để trống");
            response.put("discountPercent", 0);
            return response;
        }

        // Tìm coupon theo code (uppercase)
        Optional<Coupon> couponOpt = couponRepository.findByCode(code.toUpperCase());
        if (!couponOpt.isPresent()) {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không tồn tại");
            response.put("discountPercent", 0);
            return response;
        }

        Coupon coupon = couponOpt.get();

        // Kiểm tra coupon có active không
        if (!coupon.getIsActive()) {
            response.put("valid", false);
            response.put("message", "Mã giảm giá không còn hiệu lực");
            response.put("discountPercent", 0);
            return response;
        }

        // Kiểm tra hạn sử dụng
        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            response.put("valid", false);
            response.put("message", "Mã giảm giá đã hết hạn");
            response.put("discountPercent", 0);
            return response;
        }

        // Kiểm tra lượt sử dụng
        if (coupon.getUsageCount() >= coupon.getUsageLimit()) {
            response.put("valid", false);
            response.put("message", "Mã giảm giá đã hết lượt sử dụng");
            response.put("discountPercent", 0);
            return response;
        }

        // Coupon hợp lệ
        response.put("valid", true);
        response.put("message", "Mã giảm giá hợp lệ");
        response.put("discountPercent", coupon.getDiscountPercent());
        response.put("couponId", coupon.getCouponId());
        response.put("code", coupon.getCode());
        response.put("expiryDate", coupon.getExpiryDate());

        return response;
    }

    @Override
    public Coupon getCouponByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        Optional<Coupon> couponOpt = couponRepository.findByCode(code.toUpperCase());
        return couponOpt.orElse(null);
    }

    @Override
    public void incrementUsageCount(Integer couponId) {
        Optional<Coupon> couponOpt = couponRepository.findById(couponId);
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            coupon.setUsageCount(coupon.getUsageCount() + 1);
            coupon.setUpdatedAt(LocalDateTime.now());
            couponRepository.save(coupon);
        }
    }
}
