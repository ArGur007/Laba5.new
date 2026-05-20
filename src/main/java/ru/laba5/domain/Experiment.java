package ru.laba5.domain;

import java.time.Instant;

public final class Experiment {
    private long id;
    private String name;
    private String description;
    private String ownerUsername;
    private Instant createdAt;
    private Instant updatedAt;

    public static final int MAX_NAME_LENGTH = 128;
    public static final int MAX_DESCRIPTION_LENGTH = 512;
    public static final int MAX_OWNER_LENGTH = 64;

    // Пустой конструктор (для загрузки из БД через сеттеры)
    public Experiment() {
    }

    // Конструктор для создания НОВОГО эксперимента (без ID, ID сгенерирует БД)
    public Experiment(String name, String description, String ownerUsername) {
        validateName(name);
        validateDescription(description);
        validateOwnerUsername(ownerUsername);
        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
        this.ownerUsername = ownerUsername != null ? ownerUsername.trim() : "SYSTEM";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Experiment(long id, String name, String description, String ownerUsername) {
        this(id, name, description, ownerUsername, Instant.now(), Instant.now());
    }

    // Конструктор для ЗАГРУЗКИ из базы данных (с ID)2
    public Experiment(long id, String name, String description, String ownerUsername, Instant createdAt, Instant updatedAt) {
        validateId(id);
        validateName(name);
        validateDescription(description);
        validateOwnerUsername(ownerUsername);
        this.id = id;
        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
        this.ownerUsername = ownerUsername != null ? ownerUsername.trim() : "SYSTEM";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    // Геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { validateId(id); this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { validateName(name); this.name = name.trim(); }

    public String getDescription() { return description; }
    public void setDescription(String description) { validateDescription(description); this.description = description != null ? description.trim() : ""; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { validateOwnerUsername(ownerUsername); this.ownerUsername = ownerUsername != null ? ownerUsername.trim() : "SYSTEM"; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Вспомогательные методы для создания обновлённых копий
    public Experiment updateName(String newName) {
        validateName(newName);
        return new Experiment(this.id, newName.trim(), this.description, this.ownerUsername, this.createdAt, Instant.now());
    }

    public Experiment updateDescription(String newDescription) {
        validateDescription(newDescription);
        return new Experiment(this.id, this.name, newDescription, this.ownerUsername, this.createdAt, Instant.now());
    }

    // Валидаторы
    private static void validateId(long id) {
        if (id <= 0) throw new IllegalArgumentException("ID должен быть положительным");
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Название не может быть пустым");
        if (name.trim().length() > MAX_NAME_LENGTH)
            throw new IllegalArgumentException("Название не более " + MAX_NAME_LENGTH + " символов");
    }

    private static void validateDescription(String description) {
        if (description != null && description.length() > MAX_DESCRIPTION_LENGTH)
            throw new IllegalArgumentException("Описание не более " + MAX_DESCRIPTION_LENGTH + " символов");
    }

    private static void validateOwnerUsername(String ownerUsername) {
        if (ownerUsername != null && ownerUsername.trim().length() > MAX_OWNER_LENGTH)
            throw new IllegalArgumentException("Владелец не более " + MAX_OWNER_LENGTH + " символов");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Experiment that = (Experiment) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Experiment #%d [%s] owner=%s", id, name, ownerUsername);
    }
}