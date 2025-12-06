package com.example.FoodHKD.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.FoodItem;
import com.example.FoodHKD.model.InventoryLog;
import com.example.FoodHKD.service.InventoryLogService;

@RestController
@RequestMapping("/admin/inventory")
public class InventoryAdminController {

    @Autowired
    private InventoryLogService inventoryLogService;

    @PostMapping
    public InventoryLog createLog(@RequestBody InventoryLog log) {
        return inventoryLogService.createLog(log);
    }

    @GetMapping("/food/{foodId}")
    public List<InventoryLog> getLogs(@PathVariable Integer foodId) {
        FoodItem food = new FoodItem();
        food.setFoodID(foodId);
        return inventoryLogService.getLogsByFood(food);
    }
}
