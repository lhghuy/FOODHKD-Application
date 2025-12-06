package com.example.FoodHKD.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Error Controller - Handle 404 and other errors
 * Returns JSON instead of HTML Whitelabel Error Page
 */
@RestController
public class GlobalErrorController implements ErrorController {

    /**
     * POST /error - Handle errors
     */
    @RequestMapping("/error")
    public ResponseEntity<?> handleError() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", "NOT_FOUND");
        response.put("message", "Endpoint not found. Use REST API endpoints.");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    public String getErrorPath() {
        return "/error";
    }
}
