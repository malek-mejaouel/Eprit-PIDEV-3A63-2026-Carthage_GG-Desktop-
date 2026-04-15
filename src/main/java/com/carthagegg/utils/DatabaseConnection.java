package com.carthagegg.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mariadb://localhost:3306/carthage_gg";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getInstance() throws SQLException {
        try {
            // Return a fresh connection for each DAO operation so one screen
            // cannot accidentally close the shared connection used by another.
            Class.forName("org.mariadb.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MariaDB JDBC Driver not found", e);
        }
    }
}
