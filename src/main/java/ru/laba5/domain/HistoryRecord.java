package ru.laba5.domain;

import java.time.Instant;

public class HistoryRecord {
    private long id;
    private long experimentId;
    private String entityType;
    private long entityId;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private Instant changedAt;
    private String changedBy;

    public HistoryRecord() {}

    public HistoryRecord(long experimentId, String entityType, long entityId,
                         String fieldName, String oldValue, String newValue,
                         String changedBy) {
        this.experimentId = experimentId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedBy = changedBy;
        this.changedAt = Instant.now();
    }

    // Геттеры и сеттеры (сгенерируйте в IDE или скопируйте ниже)
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getExperimentId() { return experimentId; }
    public void setExperimentId(long experimentId) { this.experimentId = experimentId; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public long getEntityId() { return entityId; }
    public void setEntityId(long entityId) { this.entityId = entityId; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
}