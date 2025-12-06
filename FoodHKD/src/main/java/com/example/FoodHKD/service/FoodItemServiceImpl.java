package com.example.FoodHKD.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.FoodHKD.model.FoodItem;
import com.example.FoodHKD.model.InventoryLog;
import com.example.FoodHKD.repository.FoodItemRepository;
import com.example.FoodHKD.repository.InventoryLogRepository;

@Service
public class FoodItemServiceImpl implements FoodItemService {

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Override
    public List<FoodItem> getAllFoodItems() {
        return foodItemRepository.findAll();
    }

    @Override
    public FoodItem getFoodItemById(Integer id) {
        return foodItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Food item not found"));
    }

     @Override
    public FoodItem saveFoodItem(FoodItem foodItem) {
        FoodItem savedFoodItem = foodItemRepository.save(foodItem);

        // Tạo log nhập kho
        InventoryLog log = new InventoryLog();
        log.setFood(savedFoodItem);
        log.setChangeQuantity(savedFoodItem.getQuantity());  // số lượng mới nhập
        log.setNote("Nhập kho");
        log.setCreatedAt(LocalDateTime.now());

        inventoryLogRepository.save(log);

        return savedFoodItem;
    }

    @Override
    public List<FoodItem> getAvailableFoodItems() {
        return foodItemRepository.findByQuantityGreaterThan(0);
    }

   @Override
    public FoodItem createFoodItem(FoodItem foodItem) {
        FoodItem savedFoodItem = foodItemRepository.save(foodItem);

        InventoryLog log = new InventoryLog();
        log.setFood(savedFoodItem);
        log.setChangeQuantity(savedFoodItem.getQuantity());  // số lượng mới nhập
        log.setNote("tạo mới");
        log.setCreatedAt(LocalDateTime.now());

        inventoryLogRepository.save(log);

        return savedFoodItem;
    }


     @Override
    public FoodItem updateFoodItem(Integer id, FoodItem updated) {
        FoodItem foodItem = getFoodItemById(id);

        int oldQuantity = foodItem.getQuantity();

        foodItem.setName(updated.getName());
        foodItem.setDescription(updated.getDescription());
        foodItem.setPrice(updated.getPrice());
        foodItem.setAnh(updated.getAnh());
        foodItem.setCategory(updated.getCategory());
        foodItem.setQuantity(updated.getQuantity());

        FoodItem savedFoodItem = foodItemRepository.save(foodItem);

        int quantityChange = updated.getQuantity() - oldQuantity;

        if (quantityChange != 0) {
            InventoryLog log = new InventoryLog();
            log.setFood(savedFoodItem);
            log.setChangeQuantity(savedFoodItem.getQuantity()); 
            log.setNote("tạo mới");
            log.setCreatedAt(LocalDateTime.now());
            log.setCreatedBy(null);

            inventoryLogRepository.save(log);
        }

        return savedFoodItem;
    }


    @Override
    public void deleteFoodItem(Integer id) {
        getFoodItemById(id); // Kiểm tra món ăn có tồn tại
        foodItemRepository.deleteById(id);
    }


    @Override
    public List<FoodItem> searchFoodItems(String keyword) {
        return foodItemRepository.findByNameContainingIgnoreCase(keyword);
    }
}
