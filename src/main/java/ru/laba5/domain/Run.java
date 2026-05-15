package ru.laba5.domain;
import java.time.Instant;

public final class Run {
    private long id;
    private long experimentId;
    private String name;
    private String operatorName;
    private Instant createdAt;

    // Константы ограничений
    public static final int MAX_NAME_LENGTH = 128;
    public static final int MAX_OPERATOR_LENGTH = 64;

    // Конструктор для создания нового запуска (ID сгенерирует БД)
    public Run(long experimentId, String name, String operatorName) {
        validateExperimentId(experimentId);
        validateName(name);
        validateOperatorName(operatorName);
        this.experimentId = experimentId;
        this.name = name.trim();
        this.operatorName = operatorName != null ? operatorName.trim() : "SYSTEM";
        this.createdAt = Instant.now();
        // id остаётся 0, будет установлен после сохранения
    }


    // Конструктор для создания нового запуска (дата = текущее время)
    public Run(long id, long experimentId, String name, String operatorName) {
        this(id, experimentId, name, operatorName, Instant.now());
    }

    // Конструктор для загрузки из файла (с восстановлением даты)
    public Run(long id, long experimentId, String name, String operatorName, Instant createdAt) {
        validateId(id);
        validateExperimentId(experimentId);
        validateName(name);
        validateOperatorName(operatorName);

        this.id = id;
        this.experimentId = experimentId;
        this.name = name.trim();
        this.operatorName = operatorName != null ? operatorName.trim() : "SYSTEM";
        this.createdAt = createdAt;
    }

    public Run(){

    }

    // Метод для создания обновлённой копии (если нужно)
    public Run updateName(String newName) {
        validateName(newName);
        return new Run(this.id, this.experimentId, newName.trim(), this.operatorName, this.createdAt);
    }

    // Геттеры
    public long getId() { return id; }
    public long getExperimentId() { return experimentId; }
    public String getName() { return name; }
    public String getOperatorName() { return operatorName; }
    public Instant getCreatedAt() { return createdAt; }

    // Сеттеры (необходимы для OpenCSV)
    public void setId(long id) { this.id = id; }
    public void setExperimentId(long experimentId) { this.experimentId = experimentId; }
    public void setName(String name) { validateName(name); this.name = name.trim(); }
    public void setOperatorName(String operatorName) { validateOperatorName(operatorName); this.operatorName = operatorName != null ? operatorName.trim() : "SYSTEM"; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // Валидаторы
    private static void validateId(long id) {
        if (id <= 0) throw new IllegalArgumentException("ID запуска должен быть положительным");
    }

    private static void validateExperimentId(long expId) {
        if (expId <= 0) throw new IllegalArgumentException("ID эксперимента должен быть положительным");
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Название запуска не может быть пустым");
        if (name.trim().length() > MAX_NAME_LENGTH)
            throw new IllegalArgumentException("Название запуска не более " + MAX_NAME_LENGTH + " символов");
    }

    private static void validateOperatorName(String operatorName) {
        if (operatorName != null && operatorName.trim().length() > MAX_OPERATOR_LENGTH)
            throw new IllegalArgumentException("Имя оператора не более " + MAX_OPERATOR_LENGTH + " символов");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Run run = (Run) o;
        return id == run.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Run #%d [%s] experimentId=%d operator=%s", id, name, experimentId, operatorName);
    }
}