package com.carthagegg.dao;

import com.carthagegg.models.Order;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class OrderDAOTest {

    private OrderDAO service;
    private int idOrderTest;

    @BeforeAll
    void setup() {
        service = new OrderDAO();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void testAjouterOrder() {
        Order o = new Order();
        o.setUserId(1); // Assuming user 1 exists
        o.setProductId(1); // Assuming product 1 exists
        o.setQuantity(5);
        o.setStatus(Order.Status.PENDING);
        
        service.save(o);
        idOrderTest = o.getOrderId();
        
        List<Order> orders = service.findAll();
        assertFalse(orders.isEmpty());
        assertTrue(
            orders.stream().anyMatch(order -> 
                order.getOrderId() == idOrderTest)
        );
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void testModifierOrderStatus() {
        service.updateStatus(idOrderTest, Order.Status.SHIPPED);
        
        List<Order> orders = service.findAll();
        boolean trouve = orders.stream()
            .anyMatch(order -> 
                order.getOrderId() == idOrderTest && order.getStatus() == Order.Status.SHIPPED);
        assertTrue(trouve);
    }

    // No delete test as OrderDAO doesn't have a delete method implemented yet
}
