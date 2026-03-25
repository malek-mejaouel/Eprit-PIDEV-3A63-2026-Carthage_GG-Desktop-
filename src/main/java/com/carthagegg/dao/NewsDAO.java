package com.carthagegg.dao;

import com.carthagegg.models.News;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NewsDAO {
    private Connection conn;

    public NewsDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<News> findAll() throws SQLException {
        List<News> list = new ArrayList<>();
        String sql = "SELECT * FROM news"; // Removed ORDER BY published_at DESC since column might not exist or be spelled differently in actual DB
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapNews(rs));
        }
        return list;
    }

    public void save(News n) throws SQLException {
        String sql = "INSERT INTO news (title, content, published_at, category_id) VALUES (?,?,NOW(),?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getContent());
            ps.setInt(3, n.getCategoryId());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) n.setNewsId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            String fallbackSql = "INSERT INTO news (title, content, category_id) VALUES (?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(fallbackSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, n.getTitle());
                ps.setString(2, n.getContent());
                ps.setInt(3, n.getCategoryId());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) n.setNewsId(rs.getInt(1));
                }
            }
        }
    }

    public void update(News n) throws SQLException {
        String sql = "UPDATE news SET title=?, content=?, category_id=? WHERE news_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, n.getTitle());
            ps.setString(2, n.getContent());
            ps.setInt(3, n.getCategoryId());
            ps.setInt(4, n.getNewsId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM news WHERE news_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private News mapNews(ResultSet rs) throws SQLException {
        News n = new News();
        if (hasColumn(rs, "news_id")) {
            n.setNewsId(rs.getInt("news_id"));
        } else if (hasColumn(rs, "id")) {
            n.setNewsId(rs.getInt("id"));
        }
        n.setTitle(rs.getString("title"));
        n.setContent(rs.getString("content"));
        
        if (hasColumn(rs, "published_at")) {
            Timestamp ts = rs.getTimestamp("published_at");
            if (ts != null) n.setPublishedAt(ts.toLocalDateTime());
        } else if (hasColumn(rs, "created_at")) {
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) n.setPublishedAt(ts.toLocalDateTime());
        }
        
        if (hasColumn(rs, "category_id")) {
            n.setCategoryId(rs.getInt("category_id"));
        } else if (hasColumn(rs, "category")) {
            n.setCategoryId(rs.getInt("category"));
        }
        return n;
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
        }
        return false;
    }
}
