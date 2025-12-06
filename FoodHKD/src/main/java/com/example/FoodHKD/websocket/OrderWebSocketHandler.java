package com.example.FoodHKD.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * WebSocket Handler for Real-time Order Updates
 * Manages WebSocket connections and broadcasts order status changes
 */
public class OrderWebSocketHandler extends TextWebSocketHandler {

    // Store connected sessions
    private static final Set<WebSocketSession> connectedClients = Collections.synchronizedSet(new HashSet<>());
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handle new WebSocket connection
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        connectedClients.add(session);
        System.out.println("Client connected: " + session.getId());
        System.out.println("Total connected clients: " + connectedClients.size());
        
        // Send connection acknowledgment
        sendConnectionMessage(session, "Connected to Order WebSocket Service");
    }

    /**
     * Handle incoming messages from client
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            System.out.println("Received message from " + session.getId() + ": " + payload);

            // Parse incoming message
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String action = (String) data.get("action");

            if ("subscribe".equals(action)) {
                // Client is subscribing to order updates
                sendMessage(session, "success", "Subscribed to order updates", null);
            } else if ("ping".equals(action)) {
                // Respond to ping to keep connection alive
                sendMessage(session, "pong", "Pong", null);
            } else {
                // Handle other actions
                broadcastOrderUpdate(data);
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle connection close
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        connectedClients.remove(session);
        System.out.println("Client disconnected: " + session.getId());
        System.out.println("Total connected clients: " + connectedClients.size());
    }

    /**
     * Handle WebSocket errors
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket error for " + session.getId() + ": " + exception.getMessage());
        exception.printStackTrace();
        connectedClients.remove(session);
    }

    /**
     * Broadcast order status update to all connected clients
     */
    public void broadcastOrderStatusUpdate(Integer orderId, String newStatus, Map<String, Object> orderData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "orderStatusUpdate");
        message.put("orderId", orderId);
        message.put("status", newStatus);
        message.put("data", orderData);
        message.put("timestamp", System.currentTimeMillis());

        broadcastMessage(message);
    }

    /**
     * Broadcast order creation to all connected clients
     */
    public void broadcastOrderCreated(Map<String, Object> orderData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "orderCreated");
        message.put("data", orderData);
        message.put("timestamp", System.currentTimeMillis());

        broadcastMessage(message);
    }

    /**
     * Broadcast order deletion to all connected clients
     */
    public void broadcastOrderDeleted(Integer orderId) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "orderDeleted");
        message.put("orderId", orderId);
        message.put("timestamp", System.currentTimeMillis());

        broadcastMessage(message);
    }

    /**
     * Broadcast generic order update to all connected clients
     */
    public void broadcastOrderUpdate(Map<String, Object> updateData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "orderUpdate");
        message.put("data", updateData);
        message.put("timestamp", System.currentTimeMillis());

        broadcastMessage(message);
    }

    /**
     * Broadcast order item cancellation to all connected clients
     */
    public void broadcastOrderItemCancelled(Integer orderId, Integer itemId, String reason, Map<String, Object> itemData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "orderItemCancelled");
        message.put("orderId", orderId);
        message.put("itemId", itemId);
        message.put("reason", reason);
        message.put("data", itemData);
        message.put("timestamp", System.currentTimeMillis());

        broadcastMessage(message);
    }

    /**
     * Broadcast message to all connected clients
     */
    private void broadcastMessage(Map<String, Object> messageData) {
        try {
            String messagePayload = objectMapper.writeValueAsString(messageData);
            TextMessage message = new TextMessage(messagePayload);

            for (WebSocketSession session : connectedClients) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(message);
                    }
                } catch (IOException e) {
                    System.err.println("Error sending message to session " + session.getId() + ": " + e.getMessage());
                    connectedClients.remove(session);
                }
            }
        } catch (Exception e) {
            System.err.println("Error broadcasting message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send message to specific session
     */
    private void sendMessage(WebSocketSession session, String type, String message, Map<String, Object> data) throws IOException {
        if (session.isOpen()) {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", type);
            messageData.put("message", message);
            if (data != null) {
                messageData.put("data", data);
            }
            messageData.put("timestamp", System.currentTimeMillis());

            String payload = objectMapper.writeValueAsString(messageData);
            session.sendMessage(new TextMessage(payload));
        }
    }

    /**
     * Send connection acknowledgment message
     */
    private void sendConnectionMessage(WebSocketSession session, String message) throws IOException {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("type", "connected");
        messageData.put("message", message);
        messageData.put("sessionId", session.getId());
        messageData.put("timestamp", System.currentTimeMillis());

        String payload = objectMapper.writeValueAsString(messageData);
        session.sendMessage(new TextMessage(payload));
    }

    /**
     * Get number of connected clients
     */
    public int getConnectedClientsCount() {
        return connectedClients.size();
    }

    /**
     * Get the singleton instance (for broadcasting from other components)
     */
    private static OrderWebSocketHandler instance;

    public static synchronized OrderWebSocketHandler getInstance() {
        if (instance == null) {
            instance = new OrderWebSocketHandler();
        }
        return instance;
    }
}
