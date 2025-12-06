package com.example.FoodHKD.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//Thiết lập bảo mật cho các API có đường dẫn bắt đầu bằng /api/** và xử lý xác thực bằng JWT.

@Configuration
@Order(1)
public class ApiSecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf().disable()
                .sessionManagement().disable()
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/foods/image/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("QuanLy")
                        .requestMatchers("/api/employee/**").hasRole("NhanVien")
                        .anyRequest().permitAll())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}