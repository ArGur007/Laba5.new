package ru.laba5.db.dao;

import ru.laba5.db.DatabaseConnection;
import ru.laba5.users.User;
import java.sql.*;

public class UserDAO {

    public static User findByLogin(String login) throws SQLException {
        String sql = "SELECT id, login, password_hash FROM users WHERE login = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                String loginDb = rs.getString("login");
                String hash = rs.getString("password_hash");
                return new User(id, loginDb, hash);
            }
            return null;
        }
    }

    public static boolean exists(String login) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE login = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public static int create(String login, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, passwordHash);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("Failed to generate ID");
        }
    }
}