package ru.laba5.db.dao;

import ru.laba5.db.DatabaseConnection;
import ru.laba5.domain.Experiment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExperimentDAO {

    public static List<Experiment> loadAll() throws SQLException {
        String sql = "SELECT e.*, u.login as owner_login FROM experiments e JOIN users u ON e.owner_id = u.id";
        List<Experiment> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Experiment exp = new Experiment();
                exp.setId(rs.getLong("id"));
                exp.setName(rs.getString("name"));
                exp.setDescription(rs.getString("description"));
                exp.setOwnerUsername(rs.getString("owner_login"));
                exp.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                exp.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
                list.add(exp);
            }
        }
        return list;
    }

    public static int create(Experiment experiment, int ownerId) throws SQLException {
        String sql = "INSERT INTO experiments (name, description, owner_id) VALUES (?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, experiment.getName());
            stmt.setString(2, experiment.getDescription());
            stmt.setInt(3, ownerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No ID generated");
        }
    }

    public static boolean update(Experiment experiment, int ownerId) throws SQLException {
        String sql = "UPDATE experiments SET name=?, description=?, updated_at=NOW() WHERE id=? AND owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, experiment.getName());
            stmt.setString(2, experiment.getDescription());
            stmt.setLong(3, experiment.getId());
            stmt.setInt(4, ownerId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean delete(long id, int ownerId) throws SQLException {
        String sql = "DELETE FROM experiments WHERE id=? AND owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setInt(2, ownerId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static void deleteByOwner(int ownerId) throws SQLException {
        String sql = "DELETE FROM experiments WHERE owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ownerId);
            stmt.executeUpdate();
        }
    }
}