//WEBSITE CONTROLLER


// package com.example.FoodHKD.controller;

// import java.security.Principal;
// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.Authentication;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.GetMapping;

// import com.example.FoodHKD.model.User;
// import com.example.FoodHKD.service.UserService;

// @Controller
// public class PageController {

//     @Autowired
//     private UserService userService;

//     @GetMapping("/")
//     public String homePage() {
//         return "index";
//     }

//     @GetMapping("/redirect")
//     public String redirectAfterLogin(Authentication auth) {
//         if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_QuanLy"))) {
//             return "redirect:/admin";
//         } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_NhanVien"))) {
//             return "redirect:/employee";
//         } else {
//             return "redirect:/index";
//         }
//     }

//     @GetMapping("/login")
//     public String loginPage() {
//         return "login";
//     }

//     @GetMapping("/cart")
//     public String cartPage() {
//         return "cart";
//     }

//     @GetMapping("/menu")
//     public String menuPage(Model model) {
//         return "menu";
//     }

//     @GetMapping("/admin")
//     public String adminPage(Model model, Principal principal) {
//         List<User> users = userService.getAllUsers();
//         User loggedInUser = userService.getUserByUsername(principal.getName());
//         model.addAttribute("loggedInUser", loggedInUser);

//         return "admin";
//     }

//     @GetMapping("/employee")
//     public String employee() {
//         return "employee";
//     }

//     @GetMapping("/support")
//     public String supportPage() {
//         return "support";
//     }

//     @GetMapping("/status")
//     public String statusPage() {
//         return "status";
//     }

//     @GetMapping("/productmanagement")
//     public String productManagementPage() {
//         return "productmanagement";
//     }

//     @GetMapping("/revenue")
//     public String revenuePage() {
//         return "revenue";
//     }

//     @GetMapping("/staffmanagement")
//     public String staffManagementPage() {
//         return "staffmanagement";
//     }
// }