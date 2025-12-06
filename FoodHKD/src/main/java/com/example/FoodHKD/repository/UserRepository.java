package com.example.FoodHKD.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository; // Ensure the correct package path for the User class

import com.example.FoodHKD.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    
}
