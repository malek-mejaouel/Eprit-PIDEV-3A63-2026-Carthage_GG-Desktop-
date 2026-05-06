package com.carthagegg.utils;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseMigrator {
    public static void main(String[] args) {
        String sql = "ALTER TABLE matches ADD COLUMN IF NOT EXISTS is_rivalry BOOLEAN DEFAULT FALSE";
        try (Connection conn = DatabaseConnection.getInstance();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Successfully added 'is_rivalry' column to 'matches' table.");
        } catch (SQLException e) {
            System.err.println("Migration failed: " + e.getMessage());
            // If IF NOT EXISTS is not supported by the specific version, try without it
            if (e.getMessage().contains("check the manual that corresponds to your MariaDB server version")) {
                 try (Connection conn = DatabaseConnection.getInstance();
                      Statement stmt = conn.createStatement()) {
                     stmt.execute("ALTER TABLE matches ADD COLUMN is_rivalry BOOLEAN DEFAULT FALSE");
                     System.out.println("Successfully added 'is_rivalry' column (fallback).");
                 } catch (SQLException ex) {
                     if (ex.getMessage().contains("Duplicate column name")) {
                         System.out.println("Column 'is_rivalry' already exists.");
                     } else {
                         ex.printStackTrace();
                     }
                 }
            } else {
                e.printStackTrace();
            }
        }
    }
}
