package ru.laba5.service;

import ru.laba5.db.dao.ExperimentDAO;
import ru.laba5.domain.Experiment;
import ru.laba5.users.AuthService;
import java.sql.SQLException;
import java.util.*;

public class ExperimentManager {
    private final Map<Long, Experiment> cache = new HashMap<>();
    private final AuthService authService;

    public ExperimentManager(AuthService authService) {
        this.authService = authService;
        reload();
    }

    public void reload() {
        try {
            List<Experiment> list = ExperimentDAO.loadAll();
            cache.clear();
            list.forEach(e -> cache.put(e.getId(), e));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки экспериментов из БД", e);
        }
    }

    private int currentUserId() {
        return authService.getCurrentUserId();
    }

    private String currentUserLogin() {
        return authService.getCurrentUsername();
    }

    public List<Experiment> getAll() {
        return new ArrayList<>(cache.values());
    }

    // Метод для совместимости с GUI (возвращает null, если не найден)
    public Experiment findById(long id) {
        return cache.get(id);
    }

    // Optional-версия для более безопасного кода
    public Optional<Experiment> findByIdOptional(long id) {
        return Optional.ofNullable(cache.get(id));
    }

    public List<Experiment> getByOwner(String owner) {
        return cache.values().stream()
                .filter(e -> e.getOwnerUsername().equals(owner))
                .toList();
    }

    public boolean exists(long id) {
        return cache.containsKey(id);
    }

    public void add(Experiment experiment) {
        int ownerId = currentUserId();
        try {
            int newId = ExperimentDAO.create(experiment, ownerId);
            experiment.setId(newId);
            experiment.setOwnerUsername(currentUserLogin());
            cache.put(experiment.getId(), experiment);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления эксперимента", e);
        }
    }

    public void update(Experiment experiment) {
        requireOwnership(experiment.getId());
        int ownerId = currentUserId();
        try {
            if (!ExperimentDAO.update(experiment, ownerId))
                throw new SecurityException("Эксперимент не найден или не принадлежит вам");
            cache.put(experiment.getId(), experiment);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления", e);
        }
    }

    public void remove(long id) {
        requireOwnership(id);
        int ownerId = currentUserId();
        try {
            if (!ExperimentDAO.delete(id, ownerId))
                throw new SecurityException("Эксперимент не найден");
            cache.remove(id);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления", e);
        }
    }

    public void clear() {
        int ownerId = currentUserId();
        try {
            ExperimentDAO.deleteByOwner(ownerId);
            cache.values().removeIf(e -> e.getOwnerUsername().equals(currentUserLogin()));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка очистки", e);
        }
    }

    private void requireOwnership(long experimentId) {
        Experiment exp = cache.get(experimentId);
        if (exp == null) throw new IllegalArgumentException("Эксперимент #" + experimentId + " не найден");
        if (!exp.getOwnerUsername().equals(currentUserLogin()))
            throw new SecurityException("У вас нет прав на этот эксперимент");
    }
}