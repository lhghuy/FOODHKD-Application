package com.example.FoodHKD.config;

import com.example.FoodHKD.websocket.OrderWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public OrderWebSocketHandler orderWebSocketHandler() {
        return new OrderWebSocketHandler();
    }

    @Bean
    public HandshakeInterceptor ngrokHandshakeInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                         WebSocketHandler wsHandler, Map<String, Object> attributes) {
                // Log request headers for debugging
                System.out.println("WebSocket Handshake Request URI: " + request.getURI());
                request.getHeaders().forEach((key, values) -> 
                    System.out.println("Header: " + key + " = " + values));
                return true;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                                      WebSocketHandler wsHandler, Exception exception) {
                if (exception != null) {
                    System.err.println("WebSocket handshake error: " + exception.getMessage());
                }
            }
        };
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderWebSocketHandler(), "/api/ws/orders")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .addInterceptors(ngrokHandshakeInterceptor())
                .setAllowedOriginPatterns(
                    "http://localhost:3000",
                    "http://localhost:8080",
                    "http://localhost:8081",
                    "https://*.ngrok-free.dev",
                    "https://*.ngrok.io",
                    ".*"
                )
                .withSockJS();
    }
}
