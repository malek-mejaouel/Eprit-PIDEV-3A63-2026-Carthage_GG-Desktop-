package com.carthagegg.models;

import java.time.LocalDateTime;

public class Order {
    public enum Status { PENDING, SHIPPED, DELIVERED, CANCELLED }

    private int orderId;
    private int userId;
    private int productId;
    private int quantity;
    private Status status;
    private LocalDateTime orderDate;

    public Order() {}

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
}
//Rayen Omri