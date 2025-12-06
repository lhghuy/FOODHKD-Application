package com.example.FoodHKD.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.FoodHKD.model.Category; 
import com.example.FoodHKD.model.FoodItem; 

public interface FoodItemRepository extends JpaRepository<FoodItem, Integer> {
    List<FoodItem> findByNameContainingIgnoreCase(String keyword);
    List<FoodItem> findByCategory(Category category);
    List<FoodItem> findByQuantityGreaterThan(Integer quantity);
    
    // Kiểm tra xem foodId có tồn tại trong inventory_logs không
    @Query("SELECT COUNT(il) > 0 FROM InventoryLog il WHERE il.food.foodID = :foodId")
    boolean existsInInventoryLogs(@Param("foodId") Integer foodId);
    
    // Kiểm tra xem foodId có tồn tại trong order_details không
    @Query("SELECT COUNT(od) > 0 FROM OrderDetail od WHERE od.foodItem.foodID = :foodId")
    boolean existsInOrderDetails(@Param("foodId") Integer foodId);
}
