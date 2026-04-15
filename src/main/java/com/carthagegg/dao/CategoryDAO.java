package com.carthagegg.dao;

import com.carthagegg.models.Category;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private Connection conn;

    public CategoryDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapCategory(rs));
        }
        return list;
    }

    public void save(Category c) throws SQLException {
        String sql = "INSERT INTO categories (name, description, created_at) VALUES (?,?,NOW())";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            String fallbackSql = "INSERT INTO categories (name) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(fallbackSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, c.getName());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) c.setId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Category c) throws SQLException {
        String sqlId = "UPDATE categories SET name=?, description=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sqlId)) {
            ps.setString(1, c.getName());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
            return;
        } catch (SQLException ex) {
            String sqlCategoryId = "UPDATE categories SET name=?, description=? WHERE category_id=?";
            try (PreparedStatement ps = conn.prepareStatement(sqlCategoryId)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getDescription());
                ps.setInt(3, c.getId());
                ps.executeUpdate();
                return;
            } catch (SQLException ex2) {
                String sqlIdNoDesc = "UPDATE categories SET name=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sqlIdNoDesc)) {
                    ps.setString(1, c.getName());
                    ps.setInt(2, c.getId());
                    ps.executeUpdate();
                    return;
                } catch (SQLException ex3) {
                    String sqlCategoryIdNoDesc = "UPDATE categories SET name=? WHERE category_id=?";
                    try (PreparedStatement ps = conn.prepareStatement(sqlCategoryIdNoDesc)) {
                        ps.setString(1, c.getName());
                        ps.setInt(2, c.getId());
                        ps.executeUpdate();
                        return;
                    }
                }
            }
        }
    }

    public void delete(int id) throws SQLException {
        String sqlId = "DELETE FROM categories WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlId)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException ex) {
            String sqlCategoryId = "DELETE FROM categories WHERE category_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlCategoryId)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    public Category findByName(String name) throws SQLException {
        String sql = "SELECT * FROM categories WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapCategory(rs);
            }
        }
        return null;
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        Category c = new Category();
        if (hasColumn(rs, "id")) {
            c.setId(rs.getInt("id"));
        } else if (hasColumn(rs, "category_id")) {
            c.setId(rs.getInt("category_id"));
        }
        c.setName(rs.getString("name"));
        if (hasColumn(rs, "description")) {
            c.setDescription(rs.getString("description"));
        }
        if (hasColumn(rs, "created_at")) {
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
        }
        return c;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++) {
            String label = meta.getColumnLabel(i);
            String name = meta.getColumnName(i);
            if (columnName.equalsIgnoreCase(label) || columnName.equalsIgnoreCase(name)) {
                return true;
            }
            if (label != null) {
                int dot = label.lastIndexOf('.');
                if (dot >= 0 && columnName.equalsIgnoreCase(label.substring(dot + 1))) {
                    return true;
                }
            }
            if (name != null) {
                int dot = name.lastIndexOf('.');
                if (dot >= 0 && columnName.equalsIgnoreCase(name.substring(dot + 1))) {
                    return true;
                }
            }
        }
        return false;
    }
}
