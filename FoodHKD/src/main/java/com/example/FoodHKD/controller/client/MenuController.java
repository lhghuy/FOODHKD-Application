package com.example.FoodHKD.controller.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.Category;
import com.example.FoodHKD.model.FoodItem;
import com.example.FoodHKD.service.CategoryService;
import com.example.FoodHKD.service.FoodItemService;

@RestController
@RequestMapping("/client/menu")
public class MenuController {

    @Autowired
    private FoodItemService foodItemService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String showMenuPage(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<FoodItem> allFoods = foodItemService.getAllFoodItems();

        model.addAttribute("categories", categories);
        model.addAttribute("allFoods", allFoods);

        return "menu"; 
    }
}
