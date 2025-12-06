package com.example.FoodHKD.service;

import java.util.List;

import com.example.FoodHKD.model.FoodItem;

public interface FoodItemService {
    List<FoodItem> getAllFoodItems();
    FoodItem getFoodItemById(Integer id);
    FoodItem createFoodItem(FoodItem foodItem);
    FoodItem updateFoodItem(Integer id, FoodItem foodItem);
    void deleteFoodItem(Integer id);
    List<FoodItem> searchFoodItems(String keyword);
    FoodItem saveFoodItem(FoodItem foodItem);
    List<FoodItem> getAvailableFoodItems();
    
}
