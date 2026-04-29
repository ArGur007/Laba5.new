package ru.laba5.domain;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import ru.laba5.storage.InstantConverter;

import java.time.Instant;

public final class Experiment {
    @CsvBindByName(column = "id")
    private long id;

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "description")
    private String description;

    @CsvBindByName(column = "ownerUsername")
    private String ownerUsername;

    @CsvCustomBindByName(column = "createdAt", converter = InstantConverter.class)
    private Instant createdAt;

    @CsvCustomBindByName(column = "updatedAt", converter = InstantConverter.class)
    private Instant updatedAt;

    // Константы ограничений
    public static final int MAX_NAME_LENGTH = 128;
    public static final int MAX_DESCRIPTION_LENGTH = 512;
    public static final int MAX_OWNER_LENGTH = 64;

    // Пустой конструктор (обязателен для OpenCSV)
    public Experiment() {
    }

    // Конструктор для создания нового эксперимента
    public Experiment(long id, String name, String description, String ownerUsername) {
        this(id, name, description, ownerUsername, Instant.now(), Instant.now());
    }

    // Конструктор для загрузки из файла
    public Experiment(long id, String name, String description, String ownerUsername,
                      Instant createdAt, Instant updatedAt) {
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

    // Методы обновления
    public Experiment updateName(String newName) {
        validateName(newName);
        return new Experiment(this.id, newName.trim(), this.description, this.ownerUsername,
                this.createdAt, Instant.now());
    }

    public Experiment updateDescription(String newDescription) {
        validateDescription(newDescription);
        return new Experiment(this.id, this.name,
                newDescription != null ? newDescription.trim() : "",
                this.ownerUsername, this.createdAt, Instant.now());
    }

    // Геттеры
    public long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getOwnerUsername() { return ownerUsername; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // Сеттеры (необходимы для OpenCSV)
    public void setId(long id) { this.id = id; }
    public void setName(String name) { validateName(name); this.name = name.trim(); }
    public void setDescription(String description) { validateDescription(description); this.description = description != null ? description.trim() : ""; }
    public void setOwnerUsername(String ownerUsername) { validateOwnerUsername(ownerUsername); this.ownerUsername = ownerUsername != null ? ownerUsername.trim() : "SYSTEM"; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Валидаторы
    private static void validateId(long id) {
        if (id <= 0) throw new IllegalArgumentException("ID должен быть положительным");
    }

    private static void validateName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Название не может быть пустым");
        if (name.trim().length() > MAX_NAME_LENGTH) throw new IllegalArgumentException("Название не более " + MAX_NAME_LENGTH + " символов");
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