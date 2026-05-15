package ru.laba5.db.dao;

import ru.laba5.db.DatabaseConnection;
import ru.laba5.domain.Run;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RunDAO {

    public static List<Run> loadAll() throws SQLException {
        String sql = "SELECT r.*, u.login as owner_login FROM runs r JOIN users u ON r.owner_id = u.id";
        List<Run> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Run run = new Run();
                run.setId(rs.getLong("id"));
                run.setExperimentId(rs.getLong("experiment_id"));
                run.setName(rs.getString("name"));
                run.setOperatorName(rs.getString("operator_name"));
                run.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                list.add(run);
            }
        }
        return list;
    }

    public static int create(Run run, int ownerId) throws SQLException {
        String sql = "INSERT INTO runs (experiment_id, name, operator_name, owner_id) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, run.getExperimentId());
            stmt.setString(2, run.getName());
            stmt.setString(3, run.getOperatorName());
            stmt.setInt(4, ownerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No ID generated");
        }
    }

    public static boolean update(Run run, int ownerId) throws SQLException {
        String sql = "UPDATE runs SET name=?, operator_name=? WHERE id=? AND owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, run.getName());
            stmt.setString(2, run.getOperatorName());
            stmt.setLong(3, run.getId());
            stmt.setInt(4, ownerId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean delete(long id, int ownerId) throws SQLException {
        String sql = "DELETE FROM runs WHERE id=? AND owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setInt(2, ownerId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static void deleteByOwner(int ownerId) throws SQLException {
        String sql = "DELETE FROM runs WHERE owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ownerId);
            stmt.executeUpdate();
        }
    }
}