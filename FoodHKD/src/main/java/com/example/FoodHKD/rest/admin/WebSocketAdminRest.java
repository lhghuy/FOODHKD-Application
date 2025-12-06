package com.example.FoodHKD.rest.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.FoodHKD.websocket.OrderWebSocketService;

/**
 * REST Controller for WebSocket Management
 * Endpoints: /api/admin/websocket
 */
@RestController
@RequestMapping("/api/admin/websocket")
public class WebSocketAdminRest {

    @Autowired
    private OrderWebSocketService orderWebSocketService;

    /**
     * GET /api/admin/websocket/status - Get WebSocket connection status
     * Response: { "success": true, "connectedClients": N, "message": "..." }
     */
    @GetMapping("/status")
    public ResponseEntity<?> getWebSocketStatus() {
        try {
            int connectedClients = orderWebSocketService.getConnectedClientsCount();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("connectedClients", connectedClients);
            response.put("message", "WebSocket connection status retrieved successfully");
            response.put("endpoint", "ws://localhost:8080/api/ws/orders");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * POST /api/admin/websocket/broadcast - Broadcast custom message to all clients
     * Request: { "type": "...", "data": {...} }
     * Response: { "success": true, "message": "..." }
     */
    @PostMapping("/broadcast")
    public ResponseEntity<?> broadcastMessage(@RequestBody Map<String, Object> request) {
        try {
            String type = (String) request.get("type");
            Map<String, Object> data = (Map<String, Object>) request.get("data");
            
            if (type == null || type.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Type field is required");
                
                return ResponseEntity.badRequest().body(response);
            }
            
            
            Map<String, Object> broadcastData = new HashMap<>();
            broadcastData.put("type", type);
            broadcastData.put("data", data);
            
            orderWebSocketService.notifyOrderUpdated(broadcastData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Message broadcasted successfully");
            response.put("connectedClients", orderWebSocketService.getConnectedClientsCount());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
