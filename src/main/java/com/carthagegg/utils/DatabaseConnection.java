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
        try {
            // Ensure the driver is loaded
            Class.forName("org.mariadb.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MariaDB JDBC Driver not found. Please ensure the dependency is in pom.xml", e);
        } catch (SQLException e) {
            System.err.println("Database connection failed! URL: " + URL);
            System.err.println("Ensure MariaDB is running on port 3306 and database 'carthage_gg' exists.");
            throw e;
        }
    }
}
