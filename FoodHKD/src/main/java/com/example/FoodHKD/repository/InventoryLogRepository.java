package com.example.FoodHKD.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.FoodHKD.model.FoodItem;
import com.example.FoodHKD.model.InventoryLog; 
import com.example.FoodHKD.model.User; 

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Integer> {
    List<InventoryLog> findByFood(FoodItem food);
    List<InventoryLog> findByCreatedBy(User user);
}
