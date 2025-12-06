package com.example.FoodHKD.service;

import java.util.List;

import com.example.FoodHKD.model.FoodItem; // Ensure this is the correct package for InventoryLog
import com.example.FoodHKD.model.InventoryLog; // Ensure this is the correct package for FoodItem

public interface InventoryLogService {
    List<InventoryLog> getLogsByFood(FoodItem foodItem);
    InventoryLog createLog(InventoryLog log);
}
