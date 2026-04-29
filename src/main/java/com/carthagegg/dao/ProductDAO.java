package com.carthagegg.dao;

import com.carthagegg.models.Product;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    private Connection conn;

    public ProductDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
            ensureDiscountColumnExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureDiscountColumnExists() {
        try (Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS discount_price DECIMAL(10,2) DEFAULT NULL");
        } catch (SQLException e) {
            // Column might already exist or DB might not support IF NOT EXISTS in ALTER
        }
    }

    public List<Product> findAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapProduct(rs));
        }
        return list;
    }

    public void save(Product p) throws SQLException {
        String sql = "INSERT INTO products (name, price, discount_price, category_id, stock, image) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getName());
            ps.setBigDecimal(2, p.getPrice());
            ps.setBigDecimal(3, p.getDiscountPrice());
            ps.setInt(4, p.getCategoryId());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getImage());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
    }

    public void update(Product p) throws SQLException {
        String sql = "UPDATE products SET name=?, price=?, discount_price=?, category_id=?, stock=?, image=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setBigDecimal(2, p.getPrice());
            ps.setBigDecimal(3, p.getDiscountPrice());
            ps.setInt(4, p.getCategoryId());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getImage());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setDiscountPrice(rs.getBigDecimal("discount_price"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setStock(rs.getInt("stock"));
        p.setImage(rs.getString("image"));
        return p;
    }
}
