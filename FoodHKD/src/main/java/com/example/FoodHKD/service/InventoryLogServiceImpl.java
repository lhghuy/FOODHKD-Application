package com.example.FoodHKD.service;

import java.time.LocalDateTime;
import java.util.List;
import com.example.FoodHKD.model.InventoryLog;
import com.example.FoodHKD.model.FoodItem;
import com.example.FoodHKD.repository.InventoryLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class InventoryLogServiceImpl implements InventoryLogService {

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Override
    public List<InventoryLog> getLogsByFood(FoodItem foodItem) {
        return inventoryLogRepository.findByFood(foodItem);
    }

    @Override
    public InventoryLog createLog(InventoryLog log) {
        log.setCreatedAt(LocalDateTime.now());
        return inventoryLogRepository.save(log);
    }
}
