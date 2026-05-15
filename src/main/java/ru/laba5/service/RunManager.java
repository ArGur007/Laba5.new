package ru.laba5.service;

import ru.laba5.db.dao.RunDAO;
import ru.laba5.domain.Experiment;
import ru.laba5.domain.Run;
import ru.laba5.users.AuthService;
import java.sql.SQLException;
import java.util.*;

public class RunManager {
    private final Map<Long, Run> cache = new HashMap<>();
    private final AuthService authService;
    private final ExperimentManager experimentManager;

    public RunManager(AuthService authService, ExperimentManager experimentManager) {
        this.authService = authService;
        this.experimentManager = experimentManager;
        reload();
    }

    public void reload() {
        try {
            List<Run> list = RunDAO.loadAll();
            cache.clear();
            list.forEach(r -> cache.put(r.getId(), r));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки запусков", e);
        }
    }

    private int currentUserId() {
        return authService.getCurrentUserId();
    }

    private String currentUserLogin() {
        return authService.getCurrentUsername();
    }

    public List<Run> getAll() {
        return new ArrayList<>(cache.values());
    }

    // Метод для совместимости с GUI (возвращает null, если не найден)
    public Run findById(long id) {
        return cache.get(id);
    }

    // Optional-версия для более безопасного кода
    public Optional<Run> findByIdOptional(long id) {
        return Optional.ofNullable(cache.get(id));
    }

    public List<Run> getByExperiment(long experimentId) {
        return cache.values().stream()
                .filter(r -> r.getExperimentId() == experimentId)
                .toList();
    }

    public boolean exists(long id) {
        return cache.containsKey(id);
    }

    public void add(Run run) {
        requireExperimentOwnership(run.getExperimentId());
        int ownerId = currentUserId();
        try {
            int newId = RunDAO.create(run, ownerId);
            run.setId(newId);
            cache.put(run.getId(), run);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления запуска", e);
        }
    }

    public void update(Run run) {
        requireOwnership(run.getId());
        int ownerId = currentUserId();
        try {
            if (!RunDAO.update(run, ownerId))
                throw new SecurityException("Запуск не найден или не принадлежит вам");
            cache.put(run.getId(), run);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления", e);
        }
    }

    public void remove(long id) {
        requireOwnership(id);
        int ownerId = currentUserId();
        try {
            if (!RunDAO.delete(id, ownerId))
                throw new SecurityException("Запуск не найден");
            cache.remove(id);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления", e);
        }
    }

    public void clear() {
        int ownerId = currentUserId();
        try {
            RunDAO.deleteByOwner(ownerId);
            cache.values().removeIf(run -> {
                Experiment exp = experimentManager.findById(run.getExperimentId());
                return exp != null && exp.getOwnerUsername().equals(currentUserLogin());
            });
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка очистки", e);
        }
    }

    private void requireExperimentOwnership(long experimentId) {
        Experiment exp = experimentManager.findById(experimentId);
        if (exp == null || !exp.getOwnerUsername().equals(currentUserLogin()))
            throw new SecurityException("Эксперимент не найден или не принадлежит вам");
    }

    private void requireOwnership(long runId) {
        Run run = cache.get(runId);
        if (run == null) throw new IllegalArgumentException("Запуск #" + runId + " не найден");
        requireExperimentOwnership(run.getExperimentId());
    }
}