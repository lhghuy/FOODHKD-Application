package com.example.FoodHKD.exception;

public class FoodItemInUseException extends RuntimeException {
    private final String foodItemName;
    private final String conflictingTable;

    public FoodItemInUseException(String foodItemName, String conflictingTable) {
        super(String.format("Không thể xóa món ăn '%s' vì nó đang được sử dụng trong bảng '%s'", 
              foodItemName, conflictingTable));
        this.foodItemName = foodItemName;
        this.conflictingTable = conflictingTable;
    }

    public String getFoodItemName() {
        return foodItemName;
    }

    public String getConflictingTable() {
        return conflictingTable;
    }
}
