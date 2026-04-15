package com.carthagegg.dao;

import com.carthagegg.models.Comment;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CommentDAO {
    private Connection conn;
    private String tableName;

    public CommentDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
            this.tableName = resolveTableName();
            if (this.tableName == null) {
                this.tableName = "commentaires";
                ensureTable();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Comment> findAll() throws SQLException {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.*, u.username, u.avatar FROM " + tableName + " c LEFT JOIN users u ON c.user_id = u.user_id ORDER BY " + (hasColumn(tableName, "date_commentaire") ? "c.date_commentaire" : "c.commentaire_id") + " DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapComment(rs));
        } catch (SQLException ex) {
            // Fallback if users table doesn't have username or join fails
            String sql2 = "SELECT * FROM " + tableName + " ORDER BY " + (hasColumn(tableName, "date_commentaire") ? "date_commentaire" : "commentaire_id") + " DESC";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql2)) {
                while (rs.next()) list.add(mapComment(rs));
            } catch (SQLException ex2) {
                if (isMissingTable(ex2)) {
                    if ("commentaires".equalsIgnoreCase(tableName)) {
                        ensureTable();
                    }
                    return list;
                }
                throw ex2;
            }
        }
        return list;
    }

    public List<Comment> findByNewsId(int newsId) throws SQLException {
        List<Comment> filtered = new ArrayList<>();
        String sql = "SELECT c.*, u.username, u.avatar FROM " + tableName + " c LEFT JOIN users u ON c.user_id = u.user_id WHERE c.news_id = ? ORDER BY " + (hasColumn(tableName, "date_commentaire") ? "c.date_commentaire" : "c.commentaire_id") + " ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newsId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) filtered.add(mapComment(rs));
            }
        } catch (SQLException ex) {
            // Fallback without join
            String sql2 = "SELECT * FROM " + tableName + " WHERE news_id = ? ORDER BY " + (hasColumn(tableName, "date_commentaire") ? "date_commentaire" : "commentaire_id") + " ASC";
            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setInt(1, newsId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) filtered.add(mapComment(rs));
                }
            } catch (SQLException ex2) {
                if (isMissingTable(ex2)) {
                    return filtered;
                }
                throw ex2;
            }
        }
        return filtered;
    }

    public void save(Comment c) throws SQLException {
        String contentCol = hasColumn(tableName, "contenu") ? "contenu" : "content";
        String dateCol = hasColumn(tableName, "date_commentaire") ? "date_commentaire" : "created_at";
        String idCol = hasColumn(tableName, "commentaire_id") ? "commentaire_id" : (hasColumn(tableName, "comment_id") ? "comment_id" : "id");

        String sql = "INSERT INTO " + tableName + " (" + contentCol + ", " + dateCol + ", gif_url, upvotes, downvotes, news_id, user_id) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getRawContent());
            LocalDateTime dt = c.getDateCommentaire();
            if (dt != null) {
                ps.setTimestamp(2, Timestamp.valueOf(dt));
            } else {
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            }
            ps.setString(3, c.getGifUrl());
            ps.setInt(4, c.getUpvotes());
            ps.setInt(5, c.getDownvotes());
            ps.setInt(6, c.getNewsId());
            ps.setObject(7, c.getUserId() > 0 ? c.getUserId() : null);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setCommentaireId(rs.getInt(1));
            }
        } catch (SQLException ex) {
            if (isMissingTable(ex)) {
                ensureTable();
                throw ex;
            }
            throw ex;
        }
    }

    public void delete(int commentaireId) throws SQLException {
        String sql = "DELETE FROM " + tableName + " WHERE commentaire_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentaireId);
            ps.executeUpdate();
            return;
        } catch (SQLException ignored) {}

        String sql2 = "DELETE FROM " + tableName + " WHERE comment_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql2)) {
            ps.setInt(1, commentaireId);
            ps.executeUpdate();
            return;
        } catch (SQLException ignored) {}

        String sql3 = "DELETE FROM " + tableName + " WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql3)) {
            ps.setInt(1, commentaireId);
            ps.executeUpdate();
        }
    }

    private Comment mapComment(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        if (hasColumn(rs, "commentaire_id")) {
            c.setCommentaireId(rs.getInt("commentaire_id"));
        } else if (hasColumn(rs, "comment_id")) {
            c.setCommentaireId(rs.getInt("comment_id"));
        } else if (hasColumn(rs, "id")) {
            c.setCommentaireId(rs.getInt("id"));
        }
        if (hasColumn(rs, "contenu")) c.setContenu(rs.getString("contenu"));
        else if (hasColumn(rs, "content")) c.setContenu(rs.getString("content"));
        if (hasColumn(rs, "gif_url")) c.setGifUrl(rs.getString("gif_url"));
        if (hasColumn(rs, "upvotes")) c.setUpvotes(rs.getInt("upvotes"));
        if (hasColumn(rs, "downvotes")) c.setDownvotes(rs.getInt("downvotes"));
        if (hasColumn(rs, "news_id")) c.setNewsId(rs.getInt("news_id"));
        if (hasColumn(rs, "user_id")) c.setUserId(rs.getInt("user_id"));
        if (hasColumn(rs, "username")) c.setUsername(rs.getString("username"));
        if (hasColumn(rs, "avatar")) c.setAvatar(rs.getString("avatar"));

        if (hasColumn(rs, "date_commentaire")) {
            Timestamp ts = rs.getTimestamp("date_commentaire");
            if (ts != null) c.setDateCommentaire(ts.toLocalDateTime());
        } else if (hasColumn(rs, "created_at")) {
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) c.setDateCommentaire(ts.toLocalDateTime());
        }
        return c;
    }

    private boolean hasColumn(String table, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table, columnName)) {
            if (rs.next()) return true;
        }
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table.toUpperCase(), columnName.toUpperCase())) {
            if (rs.next()) return true;
        }
        return false;
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

    private void ensureTable() throws SQLException {
        String ddl = "CREATE TABLE IF NOT EXISTS commentaires (" +
                "commentaire_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "contenu TEXT NOT NULL, " +
                "date_commentaire DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "gif_url VARCHAR(255), " +
                "upvotes INT DEFAULT 0, " +
                "downvotes INT DEFAULT 0, " +
                "news_id INT NOT NULL, " +
                "user_id INT, " +
                "FOREIGN KEY (news_id) REFERENCES news(news_id) ON DELETE CASCADE, " +
                "FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL" +
                ")";
        try (Statement st = conn.createStatement()) {
            st.executeUpdate(ddl);
        }
    }

    private String resolveTableName() throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        String[] candidates = new String[]{"comments", "commentaires", "commentaire"};
        for (String candidate : candidates) {
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, candidate, new String[]{"TABLE"})) {
                if (rs.next()) return candidate;
            }
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, candidate.toUpperCase(), new String[]{"TABLE"})) {
                if (rs.next()) return candidate.toUpperCase();
            }
        }
        try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String name = rs.getString("TABLE_NAME");
                if (name == null) continue;
                String lower = name.toLowerCase();
                if (lower.equals("comments") || lower.equals("commentaires") || lower.equals("commentaire")) {
                    return name;
                }
            }
        }
        return null;
    }

    private boolean isMissingTable(SQLException ex) {
        String state = ex.getSQLState();
        return "42S02".equals(state);
    }
}
