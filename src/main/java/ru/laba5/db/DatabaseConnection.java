package ru.laba5.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.getUser(),
                    DatabaseConfig.getPassword()
            );
        }
        return connection;
    }

    public static void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            conn.createStatement().execute("SELECT 1");
        }
    }
}