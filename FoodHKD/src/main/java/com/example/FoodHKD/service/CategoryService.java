package com.example.FoodHKD.service;

import java.util.List;

import com.example.FoodHKD.model.Category;

public interface CategoryService {
    List<Category> getAllCategories();
    Category getCategoryById(Integer id);
    Category createCategory(Category category);
    Category updateCategory(Integer id, Category category);
    void deleteCategory(Integer id);
}
