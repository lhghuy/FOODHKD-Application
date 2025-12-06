package com.example.FoodHKD.rest.admin;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.model.User;
import com.example.FoodHKD.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminRest {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", users);
            response.put("total", users.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // GET /api/admin/users/{id} - Get single user
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable("id") Integer id) {
        try {
            User user = userService.getUserById(id);
            if (user != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", user);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "User not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // POST /api/admin/users - Create new user
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody @Valid User user,
                                                          BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Validation failed");
            response.put("errors", getValidationErrors(result));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Set creation timestamp
            user.setCreatedAt(LocalDateTime.now());

            User createdUser = userService.createUser(user);
            response.put("success", true);
            response.put("message", "User created successfully");
            response.put("data", createdUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // PUT /api/admin/users/{id} - Update user
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable("id") Integer id,
                                                          @RequestBody @Valid User user,
                                                          BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Validation failed");
            response.put("errors", getValidationErrors(result));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            User existingUser = userService.getUserById(id);
            if (existingUser != null) {
                User updatedUser = userService.updateUser(id, user);
                response.put("success", true);
                response.put("message", "User updated successfully");
                response.put("data", updatedUser);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // DELETE /api/admin/users/{id} - Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable("id") Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            if (currentUser != null && currentUser.getUserID().equals(id)) {
                response.put("success", false);
                response.put("message", "You cannot delete your own account");
                return ResponseEntity.ok(response);
            }

            User existingUser = userService.getUserById(id);
            if (existingUser != null ) {
                userService.deleteUser(id);
                response.put("success", true);
                response.put("message", "User deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // GET /api/admin/users/roles - Get available roles (helper endpoint)
    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getAvailableRoles() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            // Common roles in restaurant management
            response.put("data", List.of("QuanLy", "NhanVien"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch roles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // GET /api/admin/users/search - Search users by username or full name
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUsers(@RequestParam("q") String query) {
        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> (user.getUsername() != null
                            && user.getUsername().toLowerCase().contains(query.toLowerCase())) ||
                            (user.getFullName() != null
                                    && user.getFullName().toLowerCase().contains(query.toLowerCase())))
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", filteredUsers);
            response.put("total", filteredUsers.size());
            response.put("query", query);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to search users: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // GET /api/admin/users/role/{role} - Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<Map<String, Object>> getUsersByRole(@PathVariable("role") String role) {
        try {
            List<User> allUsers = userService.getAllUsers();
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getRole() != null && user.getRole().equalsIgnoreCase(role))
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", filteredUsers);
            response.put("total", filteredUsers.size());
            response.put("role", role);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch users by role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // PATCH /api/admin/users/{id}/role - Update user role only
    @PatchMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(@PathVariable("id") Integer id,
                                                              @RequestBody Map<String, String> roleData) {
        Map<String, Object> response = new HashMap<>();

        try {
            String newRole = roleData.get("role");
            if (newRole == null || newRole.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Role is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User existingUser = userService.getUserById(id);
            if (existingUser != null) {
                existingUser.setRole(newRole);
                User updatedUser = userService.updateUser(id, existingUser);

                response.put("success", true);
                response.put("message", "User role updated successfully");
                response.put("data", updatedUser);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "User not found with id: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update user role: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Helper method to extract validation errors
    private Map<String, String> getValidationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return errors;
    }

    @GetMapping("current-logged-in")
    public ResponseEntity<Map<String, Object>> getCurrentLoggedInUser(Principal principal) {
        try {
            User currentUser = userService.getUserByUsername(principal.getName());
            if (currentUser != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", currentUser);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "No user is currently logged in");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to fetch current user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
