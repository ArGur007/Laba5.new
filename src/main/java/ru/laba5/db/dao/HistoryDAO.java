package ru.laba5.db.dao;

import ru.laba5.db.DatabaseConnection;
import ru.laba5.domain.HistoryRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryDAO {

    public static void save(HistoryRecord record) throws SQLException {
        String sql = "INSERT INTO history (experiment_id, entity_type, entity_id, field_name, old_value, new_value, changed_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, record.getExperimentId());
            stmt.setString(2, record.getEntityType());
            stmt.setLong(3, record.getEntityId());
            stmt.setString(4, record.getFieldName());
            stmt.setString(5, record.getOldValue());
            stmt.setString(6, record.getNewValue());
            stmt.setString(7, record.getChangedBy());
            stmt.executeUpdate();
        }
    }

    public static List<HistoryRecord> getHistoryForExperiment(long experimentId) throws SQLException {
        String sql = "SELECT id, experiment_id, entity_type, entity_id, field_name, old_value, new_value, changed_at, changed_by " +
                "FROM history WHERE experiment_id = ? ORDER BY changed_at ASC";
        List<HistoryRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, experimentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HistoryRecord rec = new HistoryRecord();
                rec.setId(rs.getLong("id"));
                rec.setExperimentId(rs.getLong("experiment_id"));
                rec.setEntityType(rs.getString("entity_type"));
                rec.setEntityId(rs.getLong("entity_id"));
                rec.setFieldName(rs.getString("field_name"));
                rec.setOldValue(rs.getString("old_value"));
                rec.setNewValue(rs.getString("new_value"));
                rec.setChangedAt(rs.getTimestamp("changed_at").toInstant());
                rec.setChangedBy(rs.getString("changed_by"));
                list.add(rec);
            }
        }
        return list;
    }
}