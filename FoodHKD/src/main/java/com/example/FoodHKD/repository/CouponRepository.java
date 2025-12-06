package com.example.FoodHKD.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.FoodHKD.model.Coupon;
import java.util.Optional;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    Optional<Coupon> findByCode(String code);
    List<Coupon> findByIsActive(Boolean isActive);
}
