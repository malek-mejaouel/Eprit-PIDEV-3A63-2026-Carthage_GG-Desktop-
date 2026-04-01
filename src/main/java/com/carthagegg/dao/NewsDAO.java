package com.carthagegg.dao;

import com.carthagegg.models.News;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewsDAO {
    private Connection conn;
    private String idColumn;
    private String titleColumn;
    private String contentColumn;
    private String categoryColumn;
    private String publishedColumn;
    private boolean hasImage;

    public NewsDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
            resolveSchema();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<News> findAll() throws SQLException {
        if (titleColumn == null) resolveSchema();
        List<News> list = new ArrayList<>();
        String sql = "SELECT * FROM news ORDER BY " + (publishedColumn != null ? publishedColumn : idColumn) + " DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapNews(rs));
        }
        return list;
    }

    public void save(News n) throws SQLException {
        if (titleColumn == null || contentColumn == null) {
            resolveSchema();
        }

        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        List<Object> params = new ArrayList<>();

        cols.append(titleColumn);
        vals.append("?");
        params.add(n.getTitle());

        cols.append(", ").append(contentColumn);
        vals.append(", ?");
        params.add(n.getContent());

        if (categoryColumn != null) {
            cols.append(", ").append(categoryColumn);
            vals.append(", ?");
            params.add(n.getCategory());
        }

        if (hasImage) {
            cols.append(", image");
            vals.append(", ?");
            params.add(n.getImage());
        }

        if (publishedColumn != null) {
            cols.append(", ").append(publishedColumn);
            vals.append(", NOW()");
        }

        String sql = "INSERT INTO news (" + cols + ") VALUES (" + vals + ")";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) n.setNewsId(rs.getInt(1));
            }
        }
    }

    public void update(News n) throws SQLException {
        if (titleColumn == null || contentColumn == null || idColumn == null) {
            resolveSchema();
        }

        StringBuilder sql = new StringBuilder("UPDATE news SET ");
        List<Object> params = new ArrayList<>();

        sql.append(titleColumn).append("=?");
        params.add(n.getTitle());

        sql.append(", ").append(contentColumn).append("=?");
        params.add(n.getContent());

        if (categoryColumn != null) {
            sql.append(", ").append(categoryColumn).append("=?");
            params.add(n.getCategory());
        }

        if (hasImage) {
            sql.append(", image=?");
            params.add(n.getImage());
        }

        sql.append(" WHERE ").append(idColumn).append("=?");
        params.add(n.getNewsId());

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                ps.setObject(i + 1, p);
            }
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        if (idColumn == null) {
            resolveSchema();
        }
        String sql = "DELETE FROM news WHERE " + (idColumn != null ? idColumn : "news_id") + " = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private News mapNews(ResultSet rs) throws SQLException {
        News n = new News();
        Integer nid = getInt(rs, "news_id", "id");
        if (nid != null) n.setNewsId(nid);

        String title = getString(rs, "title", "titre");
        n.setTitle(title);

        String content = getString(rs, "content", "contenu");
        n.setContent(content);

        String image = getString(rs, "image");
        n.setImage(image);

        Timestamp ts = getTimestamp(rs, "published_at", "date_publication", "created_at");
        if (ts != null) n.setPublishedAt(ts.toLocalDateTime());

        String cat = getString(rs, "category_id", "categorie", "category");
        n.setCategory(cat);
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

    private void resolveSchema() throws SQLException {
        Set<String> cols = getTableColumns("news");
        idColumn = cols.contains("news_id") ? "news_id" : (cols.contains("id") ? "id" : "news_id");
        titleColumn = cols.contains("title") ? "title" : (cols.contains("titre") ? "titre" : null);
        contentColumn = cols.contains("content") ? "content" : (cols.contains("contenu") ? "contenu" : null);
        categoryColumn = cols.contains("category_id") ? "category_id" : (cols.contains("categorie") ? "categorie" : (cols.contains("category") ? "category" : null));
        publishedColumn = cols.contains("published_at") ? "published_at" : (cols.contains("date_publication") ? "date_publication" : (cols.contains("created_at") ? "created_at" : null));
        hasImage = cols.contains("image");
    }

    private Set<String> getTableColumns(String table) throws SQLException {
        Set<String> cols = new HashSet<>();
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table, "%")) {
            while (rs.next()) {
                String c = rs.getString("COLUMN_NAME");
                if (c != null) cols.add(c.toLowerCase());
            }
        }
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, table.toUpperCase(), "%")) {
            while (rs.next()) {
                String c = rs.getString("COLUMN_NAME");
                if (c != null) cols.add(c.toLowerCase());
            }
        }
        return cols;
    }

    private String resolveActualColumn(ResultSet rs, String candidate) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int count = meta.getColumnCount();
        for (int i = 1; i <= count; i++) {
            String label = meta.getColumnLabel(i);
            String name = meta.getColumnName(i);
            if (candidate.equalsIgnoreCase(label) || candidate.equalsIgnoreCase(name)) return label != null ? label : name;
            if (label != null) {
                int dot = label.lastIndexOf('.');
                if (dot >= 0 && candidate.equalsIgnoreCase(label.substring(dot + 1))) return label;
            }
            if (name != null) {
                int dot = name.lastIndexOf('.');
                if (dot >= 0 && candidate.equalsIgnoreCase(name.substring(dot + 1))) return name;
            }
        }
        return null;
    }

    private String getString(ResultSet rs, String... candidates) throws SQLException {
        for (String c : candidates) {
            String actual = resolveActualColumn(rs, c);
            if (actual != null) return rs.getString(actual);
        }
        return null;
    }

    private Integer getInt(ResultSet rs, String... candidates) throws SQLException {
        for (String c : candidates) {
            String actual = resolveActualColumn(rs, c);
            if (actual != null) {
                int v = rs.getInt(actual);
                return rs.wasNull() ? null : v;
            }
        }
        return null;
    }

    private Timestamp getTimestamp(ResultSet rs, String... candidates) throws SQLException {
        for (String c : candidates) {
            String actual = resolveActualColumn(rs, c);
            if (actual != null) return rs.getTimestamp(actual);
        }
        return null;
    }
}
