package com.carthagegg.dao;

import com.carthagegg.models.Game;
import com.carthagegg.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameDAO {
    private Connection conn;

    public GameDAO() {
        try {
            this.conn = DatabaseConnection.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Game> findAll() throws SQLException {
        List<Game> list = new ArrayList<>();
        String sql = "SELECT * FROM games";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapGame(rs));
        }
        return list;
    }

    public Game findById(int id) throws SQLException {
        String sql = "SELECT * FROM games WHERE game_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapGame(rs);
            }
        }
        return null;
    }

    public void save(Game game) throws SQLException {
        String sql = "INSERT INTO games (name, genre, description) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, game.getName());
            ps.setString(2, game.getGenre());
            ps.setString(3, game.getDescription());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) game.setGameId(rs.getInt(1));
            }
        }
    }

    public void update(Game game) throws SQLException {
        String sql = "UPDATE games SET name=?, genre=?, description=? WHERE game_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.getName());
            ps.setString(2, game.getGenre());
            ps.setString(3, game.getDescription());
            ps.setInt(4, game.getGameId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM games WHERE game_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Game mapGame(ResultSet rs) throws SQLException {
        Game g = new Game();
        g.setGameId(rs.getInt("game_id"));
        g.setName(rs.getString("name"));
        g.setGenre(rs.getString("genre"));
        g.setDescription(rs.getString("description"));
        return g;
    }
}
