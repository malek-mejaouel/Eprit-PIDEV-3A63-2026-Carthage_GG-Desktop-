package com.carthagegg.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mariadb://localhost:3306/carthage_gg";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static Connection instance;

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            try {
                // Ensure the driver is loaded
                Class.forName("org.mariadb.jdbc.Driver");
                instance = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MariaDB JDBC Driver not found", e);
            }
        }
        return instance;
    }
}
