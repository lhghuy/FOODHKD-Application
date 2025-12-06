package com.example.FoodHKD.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirect Controller - For Flutter Web SPA
 * Redirects all non-API routes to index.html for Flutter routing
 */
@Controller
public class RedirectController {

    @GetMapping("/login")
    public String redirectLogin() {
        return "redirect:/";
    }

    @GetMapping("/admin")
    public String redirectAdmin() {
        return "redirect:/";
    }

    @GetMapping("/employee")
    public String redirectEmployee() {
        return "redirect:/";
    }

    @GetMapping("/menu")
    public String redirectMenu() {
        return "redirect:/";
    }

    @GetMapping("/cart")
    public String redirectCart() {
        return "redirect:/";
    }
}
