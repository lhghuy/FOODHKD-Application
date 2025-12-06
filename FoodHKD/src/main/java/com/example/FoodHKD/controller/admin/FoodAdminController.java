package com.example.FoodHKD.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.FoodItem;
import com.example.FoodHKD.service.CategoryService;
import com.example.FoodHKD.service.FoodItemService;

@RestController
@RequestMapping("/admin/foods")
public class FoodAdminController {

    @Autowired
    private FoodItemService foodItemService;

    @Autowired
    private CategoryService categoryService;

    // Hiển thị danh sách món ăn
    @GetMapping
    public String getAll(Model model) {
        model.addAttribute("foods", foodItemService.getAllFoodItems());
        return "productmanagement";
    }

    // Hiển thị form thêm món ăn
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("food", new FoodItem());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "productform";
    }

    // Hiển thị form sửa món ăn
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {
        model.addAttribute("food", foodItemService.getFoodItemById(id));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "productform";
    }

    // Xử lý thêm món ăn
    @PostMapping("/add")
    public String addFood(@ModelAttribute("food") FoodItem food) {
        foodItemService.createFoodItem(food);
        return "redirect:/admin/foods";
    }

    // Xử lý cập nhật món ăn
    @PostMapping("/update")
    public String updateFood(@ModelAttribute("food") FoodItem food) {
        foodItemService.updateFoodItem(food.getFoodID(), food);
        return "redirect:/admin/foods";
    }
}
