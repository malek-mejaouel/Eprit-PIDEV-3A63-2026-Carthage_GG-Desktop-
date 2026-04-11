package com.carthagegg.dao;

import com.carthagegg.models.Order;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private Connection conn;

    public OrderDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Order> findAll() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapOrder(rs));
        }
        return list;
    }

    public void save(Order o) throws SQLException {
        String sql = "INSERT INTO orders (user_id, product_id, quantity, status, order_date) VALUES (?,?,?,?,NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, o.getUserId());
            ps.setInt(2, o.getProductId());
            ps.setInt(3, o.getQuantity());
            ps.setString(4, o.getStatus().name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) o.setOrderId(rs.getInt(1));
            }
        }
    }

    public void updateStatus(int orderId, Order.Status status) throws SQLException {
        String sql = "UPDATE orders SET status=? WHERE order_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        o.setUserId(rs.getInt("user_id"));
        o.setProductId(rs.getInt("product_id"));
        o.setQuantity(rs.getInt("quantity"));
        o.setStatus(Order.Status.valueOf(rs.getString("status")));
        o.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        return o;
    }
}
