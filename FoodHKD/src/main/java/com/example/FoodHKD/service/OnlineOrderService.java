package com.example.FoodHKD.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.FoodHKD.model.OnlineOrder;
import com.example.FoodHKD.model.OnlineOrderItem;
import com.example.FoodHKD.repository.OnlineOrderRepository;
import com.example.FoodHKD.repository.OnlineOrderItemRepository;
import com.example.FoodHKD.websocket.OrderWebSocketService;

/**
 * Service layer for Online Order Management
 * Handles business logic for order operations
 */
@Service
public class OnlineOrderService {

    @Autowired
    private OnlineOrderRepository onlineOrderRepository;

    @Autowired
    private OnlineOrderItemRepository onlineOrderItemRepository;

    @Autowired
    private OrderWebSocketService orderWebSocketService;

    /**
     * Create a new online order
     */
    public OnlineOrder createOrder(Integer customerId, String customerName, String phone, 
                                   List<OnlineOrderItem> items) throws Exception {
        try {
            // Validate input
            if (customerId == null || customerName == null || items == null || items.isEmpty()) {
                throw new IllegalArgumentException("Invalid order data");
            }

            // Create order
            OnlineOrder order = new OnlineOrder();
            order.setCustomerId(customerId);
            order.setCustomerName(customerName);
            order.setPhone(phone != null ? phone : "");
            order.setStatus("Pending");
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            // Calculate total
            BigDecimal total = BigDecimal.ZERO;
            for (OnlineOrderItem item : items) {
                item.setOnlineOrder(order);
                total = total.add(item.getPriceAtOrderTime().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            order.setTotal(total);
            order.setItems(items);

            // Save order
            OnlineOrder savedOrder = onlineOrderRepository.save(order);

            return savedOrder;
        } catch (Exception e) {
            throw new Exception("Error creating order: " + e.getMessage());
        }
    }

    /**
     * Get all orders
     */
    public List<OnlineOrder> getAllOrders() throws Exception {
        try {
            return onlineOrderRepository.findAll();
        } catch (Exception e) {
            throw new Exception("Error fetching orders: " + e.getMessage());
        }
    }

    /**
     * Get order by ID
     */
    public OnlineOrder getOrderById(Integer orderId) throws Exception {
        try {
            Optional<OnlineOrder> order = onlineOrderRepository.findById(orderId);
            if (!order.isPresent()) {
                throw new Exception("Order not found");
            }
            return order.get();
        } catch (Exception e) {
            throw new Exception("Error fetching order: " + e.getMessage());
        }
    }

    /**
     * Get orders by customer ID
     */
    public List<OnlineOrder> getOrdersByCustomerId(Integer customerId) throws Exception {
        try {
            return onlineOrderRepository.findByCustomerId(customerId);
        } catch (Exception e) {
            throw new Exception("Error fetching customer orders: " + e.getMessage());
        }
    }

    /**
     * Get orders by status
     */
    public List<OnlineOrder> getOrdersByStatus(String status) throws Exception {
        try {
            return onlineOrderRepository.findByStatus(status);
        } catch (Exception e) {
            throw new Exception("Error fetching orders by status: " + e.getMessage());
        }
    }

    /**
     * Update order status
     */
    public OnlineOrder updateOrderStatus(Integer orderId, String newStatus) throws Exception {
        try {
            Optional<OnlineOrder> orderOpt = onlineOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                throw new Exception("Order not found");
            }

            OnlineOrder order = orderOpt.get();
            order.setStatus(newStatus);
            order.setUpdatedAt(LocalDateTime.now());

            OnlineOrder updatedOrder = onlineOrderRepository.save(order);

            return updatedOrder;
        } catch (Exception e) {
            throw new Exception("Error updating order status: " + e.getMessage());
        }
    }

    /**
     * Add item to order
     */
    public OnlineOrderItem addItemToOrder(Integer orderId, Integer foodId, String foodName, 
                                         Integer quantity, BigDecimal price, String imageUrl) throws Exception {
        try {
            Optional<OnlineOrder> orderOpt = onlineOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                throw new Exception("Order not found");
            }

            OnlineOrder order = orderOpt.get();

            // Create item
            OnlineOrderItem item = new OnlineOrderItem(foodId, foodName, quantity, price, imageUrl);
            item.setOnlineOrder(order);

            // Save item
            OnlineOrderItem savedItem = onlineOrderItemRepository.save(item);

            // Update order total
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(quantity));
            order.setTotal(order.getTotal().add(itemTotal));
            order.setUpdatedAt(LocalDateTime.now());
            onlineOrderRepository.save(order);

            return savedItem;
        } catch (Exception e) {
            throw new Exception("Error adding item to order: " + e.getMessage());
        }
    }

    /**
     * Delete order
     */
    public void deleteOrder(Integer orderId) throws Exception {
        try {
            Optional<OnlineOrder> orderOpt = onlineOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                throw new Exception("Order not found");
            }

            onlineOrderRepository.deleteById(orderId);
        } catch (Exception e) {
            throw new Exception("Error deleting order: " + e.getMessage());
        }
    }

    /**
     * Get items for an order
     */
    public List<OnlineOrderItem> getOrderItems(Integer orderId) throws Exception {
        try {
            return onlineOrderItemRepository.findByOnlineOrderOrderId(orderId);
        } catch (Exception e) {
            throw new Exception("Error fetching order items: " + e.getMessage());
        }
    }

    /**
     * Cancel an item in the order
     */
    public OnlineOrderItem cancelOrderItem(Integer orderId, Integer itemId, String reason) throws Exception {
        try {
            Optional<OnlineOrder> orderOpt = onlineOrderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                throw new Exception("Order not found");
            }

            Optional<OnlineOrderItem> itemOpt = onlineOrderItemRepository.findById(itemId);
            if (!itemOpt.isPresent()) {
                throw new Exception("Item not found");
            }

            OnlineOrderItem item = itemOpt.get();
            
            // Verify item belongs to this order
            if (!item.getOnlineOrder().getOrderId().equals(orderId)) {
                throw new Exception("Item does not belong to this order");
            }

            // Cancel the item
            item.setStatus("Cancelled");
            item.setCancelReason(reason != null ? reason : "");

            OnlineOrderItem cancelledItem = onlineOrderItemRepository.save(item);

            // Update order total
            OnlineOrder order = orderOpt.get();
            BigDecimal cancelledAmount = item.getPriceAtOrderTime().multiply(BigDecimal.valueOf(item.getQuantity()));
            order.setTotal(order.getTotal().subtract(cancelledAmount));
            order.setUpdatedAt(LocalDateTime.now());
            onlineOrderRepository.save(order);

            return cancelledItem;
        } catch (Exception e) {
            throw new Exception("Error cancelling item: " + e.getMessage());
        }
    }
}
