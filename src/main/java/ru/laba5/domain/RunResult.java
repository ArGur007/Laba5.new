package ru.laba5.domain;

import java.time.Instant;

public final class RunResult {
    private long id;
    private long runId;
    private MeasurementParam param;
    private double value;
    private String unit;
    private String comment;
    private String ownerUsername;
    private Instant createdAt;

    // Константы ограничений
    public static final int MAX_UNIT_LENGTH = 16;
    public static final int MAX_COMMENT_LENGTH = 128;
    public static final int MAX_OWNER_LENGTH = 64;


    public RunResult() {

    }

    // Конструктор для создания нового результата (ID сгенерирует БД)
    public RunResult(long runId, MeasurementParam param, double value, String unit, String comment, String ownerUsername) {
        validateRunId(runId);
        validateParam(param);
        validateValue(value);
        validateUnit(unit);
        validateComment(comment);
        validateOwner(ownerUsername);
        this.runId = runId;
        this.param = param;
        this.value = value;
        this.unit = unit != null ? unit.trim() : "";
        this.comment = comment != null ? comment.trim() : "";
        this.ownerUsername = ownerUsername != null ? ownerUsername.trim() : "SYSTEM";
        this.createdAt = Instant.now();
        // id = 0, будет установлен после сохранения
    }

    // Конструктор для создания нового результата (дата = текущее время)
    public RunResult(long id, long runId, MeasurementParam param, double value,
                     String unit, String comment, String ownerUsername) {
        this(id, runId, param, value, unit, comment, ownerUsername, Instant.now());
    }

    // Конструктор для загрузки из файла (с восстановлением даты)
    public RunResult(long id, long runId, MeasurementParam param, double value,
                     String unit, String comment, String ownerUsername, Instant createdAt) {
        validateId(id);
        validateRunId(runId);
        validateParam(param);
        validateValue(value);
        validateUnit(unit);
        validateComment(comment);
        validateOwner(ownerUsername);

        this.id = id;
        this.runId = runId;
        this.param = param;
        this.value = value;
        this.unit = unit != null ? unit.trim() : "";
        this.comment = comment != null ? comment.trim() : "";
        this.ownerUsername = ownerUsername != null ? ownerUsername.trim() : "SYSTEM";
        this.createdAt = createdAt;
    }

    // Геттеры
    public long getId() { return id; }
    public long getRunId() { return runId; }
    public MeasurementParam getParam() { return param; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public String getComment() { return comment; }
    public String getOwnerUsername() { return ownerUsername; }
    public Instant getCreatedAt() { return createdAt; }

    // Сеттеры (необходимы для OpenCSV)
    public void setId(long id) { this.id = id; }
    public void setRunId(long runId) { this.runId = runId; }
    public void setParam(MeasurementParam param) { validateParam(param); this.param = param; }
    public void setValue(double value) { validateValue(value); this.value = value; }
    public void setUnit(String unit) { validateUnit(unit); this.unit = unit != null ? unit.trim() : ""; }
    public void setComment(String comment) { validateComment(comment); this.comment = comment != null ? comment.trim() : ""; }
    public void setOwnerUsername(String ownerUsername) { validateOwner(ownerUsername); this.ownerUsername = ownerUsername != null ? ownerUsername.trim() : "SYSTEM"; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Валидаторы
    private static void validateId(long id) {
        if (id <= 0) throw new IllegalArgumentException("ID результата должен быть положительным");
    }

    private static void validateRunId(long runId) {
        if (runId <= 0) throw new IllegalArgumentException("ID запуска должен быть положительным");
    }

    private static void validateParam(MeasurementParam param) {
        if (param == null) throw new IllegalArgumentException("Параметр не может быть null");
    }

    private static void validateValue(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value))
            throw new IllegalArgumentException("Значение должно быть конечным числом");
    }

    private static void validateUnit(String unit) {
        if (unit == null || unit.trim().isEmpty())
            throw new IllegalArgumentException("Единицы измерения не могут быть пустыми");
        if (unit.trim().length() > MAX_UNIT_LENGTH)
            throw new IllegalArgumentException("Единицы не более " + MAX_UNIT_LENGTH + " символов");
    }

    private static void validateComment(String comment) {
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH)
            throw new IllegalArgumentException("Комментарий не более " + MAX_COMMENT_LENGTH + " символов");
    }

    private static void validateOwner(String owner) {
        if (owner != null && owner.trim().length() > MAX_OWNER_LENGTH)
            throw new IllegalArgumentException("Имя владельца не более " + MAX_OWNER_LENGTH + " символов");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunResult that = (RunResult) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("RunResult #%d: %s = %.2f %s", id, param, value, unit);
    }
}