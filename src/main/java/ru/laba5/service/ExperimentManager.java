package ru.laba5.service;

import ru.laba5.db.dao.ExperimentDAO;
import ru.laba5.domain.Experiment;
import ru.laba5.users.AuthService;
import java.sql.SQLException;
import java.util.*;
import ru.laba5.db.dao.HistoryDAO;
import ru.laba5.domain.HistoryRecord;

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

    public Experiment findById(long id) {
        return cache.get(id);
    }

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

            HistoryRecord rec = new HistoryRecord(
                    experiment.getId(),
                    "experiment",experiment.getId(), "creatoin",
                    null,
                    "Эксперимент создан",
                    currentUserLogin()
            );
            HistoryDAO.save(rec);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления эксперимента", e);
        }
    }
    public void update(Experiment experiment) {
        Experiment old = cache.get(experiment.getId());
        if (old == null) throw new IllegalArgumentException("Эксперимент не найден");
        requireOwnership(experiment.getId());
        List<HistoryRecord> changes = new ArrayList<>();

        // Проверяем изменение названия
        if (!old.getName().equals(experiment.getName())) {
            changes.add(new HistoryRecord(experiment.getId(),"experiment",
                    experiment.getId(),
                    "name",
                    old.getName(),
                    experiment.getName(),
                    currentUserLogin()
            ));
        }
        if (!old.getDescription().equals(experiment.getDescription())){
            changes.add(new HistoryRecord(experiment.getId(), "experiment", experiment.getId(), "description", old.getDescription(), experiment.getDescription(), currentUserLogin()));
        }

        int ownerId = currentUserId();
        try {
            if (!ExperimentDAO.update(experiment, ownerId))
                throw new SecurityException("Эксперимент не найден или не принадлежит вам");
            cache.put(experiment.getId(), experiment);
            for (HistoryRecord rec : changes) {
                HistoryDAO.save(rec);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления", e);
        }
    }
    public void remove(long id) {
        Experiment exp = cache.get(id);
        if (exp == null) throw new IllegalArgumentException("Эксперимент #" + id + " не найден");
        requireOwnership(id);

        int ownerId = currentUserId();
        try {
            // Сохраняем запись об удалении в историю
            HistoryRecord deletionRecord = new HistoryRecord(
                    id,
                    "experiment",
                    id,
                    "deletion",
                    null,
                    "Эксперимент удалён",
                    currentUserLogin()
            );
            HistoryDAO.save(deletionRecord);

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