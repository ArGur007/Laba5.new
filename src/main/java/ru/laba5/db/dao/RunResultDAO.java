package ru.laba5.db.dao;

import ru.laba5.db.DatabaseConnection;
import ru.laba5.domain.MeasurementParam;
import ru.laba5.domain.RunResult;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RunResultDAO {

    public static List<RunResult> loadAll() throws SQLException {
        String sql = "SELECT rr.*, u.login as owner_login FROM run_results rr JOIN users u ON rr.owner_id = u.id";
        List<RunResult> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                RunResult result = new RunResult();
                result.setId(rs.getLong("id"));
                result.setRunId(rs.getLong("run_id"));
                result.setParam(MeasurementParam.valueOf(rs.getString("param")));
                result.setValue(rs.getDouble("value"));
                result.setUnit(rs.getString("unit"));
                result.setComment(rs.getString("comment"));
                result.setOwnerUsername(rs.getString("owner_login"));
                result.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                list.add(result);
            }
        }
        return list;
    }

    public static int create(RunResult result, int ownerId) throws SQLException {
        String sql = "INSERT INTO run_results (run_id, param, value, unit, comment, owner_id) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, result.getRunId());
            stmt.setString(2, result.getParam().name());
            stmt.setDouble(3, result.getValue());
            stmt.setString(4, result.getUnit());
            stmt.setString(5, result.getComment());
            stmt.setInt(6, ownerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("No ID generated");
        }
    }

    public static boolean update(RunResult result, int ownerId) throws SQLException {
        String sql = "UPDATE run_results SET value=?, comment=?, param=?, unit=? WHERE id=? AND owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, result.getValue());
            stmt.setString(2, result.getComment());
            stmt.setString(3, result.getParam().name());   // сохраняем параметр
            stmt.setString(4, result.getUnit());            // сохраняем единицы
            stmt.setLong(5, result.getId());
            stmt.setInt(6, ownerId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static boolean delete(long id, int ownerId) throws SQLException {
        String sql = "DELETE FROM run_results WHERE id=? AND owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setInt(2, ownerId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static void deleteByOwner(int ownerId) throws SQLException {
        String sql = "DELETE FROM run_results WHERE owner_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ownerId);
            stmt.executeUpdate();
        }
    }
}