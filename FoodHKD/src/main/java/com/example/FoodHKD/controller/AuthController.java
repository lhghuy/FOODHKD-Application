package com.example.FoodHKD.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.User;
import com.example.FoodHKD.service.UserService;

@RestController
@RequestMapping("/api/client/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String fullName;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        // Kiểm tra user tồn tại
        User user = userService.getUserByUsername(request.getUsername());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Tài khoản không tồn tại"));
        }

        // Kiểm tra mật khẩu
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Mật khẩu không đúng"));
        }

        // Tạo JWT token
        String token = userService.loginGetToken(request.getUsername(), request.getPassword());

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Không thể tạo token đăng nhập"));
        }

        // Trả JSON về cho Flutter
        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "userId", user.getUserID(),
                        "username", user.getUsername(),
                        "role", user.getRole(),
                        "fullName", user.getFullName(),
                        "createdAt", user.getCreatedAt()
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Validate input
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Username không được để trống"));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Password không được để trống"));
            }

            if (request.getPassword().length() < 6) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Password phải ít nhất 6 ký tự"));
            }

            // Gọi service để register
            User newUser = userService.registerCustomer(
                    request.getUsername(),
                    request.getPassword(),
                    request.getFullName()
            );

            // Trả về success
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message", "Đăng ký thành công",
                            "userId", newUser.getUserID(),
                            "username", newUser.getUsername(),
                            "role", newUser.getRole(),
                            "fullName", newUser.getFullName(),
                            "createdAt", newUser.getCreatedAt()
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
