package com.example.FoodHKD.service;

import java.util.List;

import com.example.FoodHKD.model.User;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(Integer id);

    User getUserByUsername(String username);

    User createUser(User user);

    User updateUser(Integer id, User user);

    void deleteUser(Integer id);

    String loginGetToken(String username, String password);

    User registerCustomer(String username, String password, String fullName);
}
