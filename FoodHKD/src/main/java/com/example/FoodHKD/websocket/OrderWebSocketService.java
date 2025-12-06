package com.example.FoodHKD.websocket;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for broadcasting WebSocket messages to all connected clients
 * Provides convenient methods for different types of order updates
 */
@Service
public class OrderWebSocketService {

    private final OrderWebSocketHandler webSocketHandler;

    public OrderWebSocketService() {
        this.webSocketHandler = OrderWebSocketHandler.getInstance();
    }

    /**
     * Broadcast order status change
     */
    public void notifyOrderStatusChanged(Integer orderId, String newStatus, Map<String, Object> orderData) {
        webSocketHandler.broadcastOrderStatusUpdate(orderId, newStatus, orderData);
    }

    /**
     * Broadcast new order creation
     */
    public void notifyOrderCreated(Map<String, Object> orderData) {
        webSocketHandler.broadcastOrderCreated(orderData);
    }

    /**
     * Broadcast order deletion
     */
    public void notifyOrderDeleted(Integer orderId) {
        webSocketHandler.broadcastOrderDeleted(orderId);
    }

    /**
     * Broadcast generic order update
     */
    public void notifyOrderUpdated(Map<String, Object> updateData) {
        webSocketHandler.broadcastOrderUpdate(updateData);
    }

    /**
     * Broadcast order item cancellation
     */
    public void notifyOrderItemCancelled(Integer orderId, Integer itemId, String reason, Map<String, Object> itemData) {
        webSocketHandler.broadcastOrderItemCancelled(orderId, itemId, reason, itemData);
    }

    /**
     * Get current number of connected clients
     */
    public int getConnectedClientsCount() {
        return webSocketHandler.getConnectedClientsCount();
    }
}
