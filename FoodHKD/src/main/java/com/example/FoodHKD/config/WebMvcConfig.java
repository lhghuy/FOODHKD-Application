package com.example.FoodHKD.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy absolute path để tránh issues với relative path
        String uploadDir = System.getProperty("user.home") + "/FoodHKD_uploads/";
        
        // Map thư mục uploads thành URL public
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
        
        // Map cả thư mục con foods
        registry.addResourceHandler("/uploads/foods/**")
                .addResourceLocations("file:" + uploadDir + "foods/");
    }
}
