package com.example.FoodHKD.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.FoodHKD.exception.FoodItemInUseException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Xử lý exception khi xóa FoodItem đang được sử dụng
     */
    @ExceptionHandler(FoodItemInUseException.class)
    public ResponseEntity<Map<String, Object>> handleFoodItemInUseException(FoodItemInUseException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("errorCode", "FOOD_ITEM_IN_USE");
        response.put("foodItemName", ex.getFoodItemName());
        response.put("conflictingTable", ex.getConflictingTable());
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Xử lý exception chung cho RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("errorCode", "INTERNAL_ERROR");
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Xử lý exception chung cho tất cả exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Đã xảy ra lỗi: " + ex.getMessage());
        response.put("errorCode", "UNKNOWN_ERROR");
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
