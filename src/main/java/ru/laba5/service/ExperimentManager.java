package ru.laba5.service;

import ru.laba5.domain.Experiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExperimentManager {
    private final Map<Long, Experiment> experiments = new HashMap<>();
    private final IdGenerator idGenerator = new IdGenerator();

    public long getNextId() {
        return idGenerator.nextId();
    }

    public void add(Experiment exp) {
        if (experiments.containsKey(exp.getId())) {
            throw new IllegalArgumentException("Experiment ID " + exp.getId() + " already exists");
        }
        experiments.put(exp.getId(), exp);
    }

    public Experiment findById(long id) {
        return experiments.get(id);
    }

    public List<Experiment> getAll() {
        return new ArrayList<>(experiments.values());
    }

    public List<Experiment> getByOwner(String owner) {
        if (owner == null) return new ArrayList<>();
        return experiments.values().stream()
                .filter(e -> owner.equals(e.getOwnerUsername()))
                .collect(Collectors.toList());
    }

    public void update(Experiment exp, String currentUser) {
        Experiment existing = experiments.get(exp.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Experiment #" + exp.getId() + " not found");
        }
        if (!existing.getOwnerUsername().equals(currentUser)) {
            throw new SecurityException("У вас нет прав на изменение этого эксперимента (владелец: " + existing.getOwnerUsername() + ")");
        }
        experiments.put(exp.getId(), exp);
    }

    public void remove(long id, String currentUser) {
        Experiment existing = experiments.get(id);
        if (existing == null) {
            throw new IllegalArgumentException("Experiment #" + id + " not found");
        }
        if (!existing.getOwnerUsername().equals(currentUser)) {
            throw new SecurityException("У вас нет прав на удаление этого эксперимента (владелец: " + existing.getOwnerUsername() + ")");
        }
        experiments.remove(id);
    }

    public boolean exists(long id) {
        return experiments.containsKey(id);
    }

    public void clear() {
        experiments.clear();
    }

    public void syncIdGenerator() {
        long maxId = experiments.keySet().stream()
                .max(Long::compareTo)
                .orElse(0L);
        idGenerator.syncWithMaxId(maxId);
    }

    public boolean isExperimentBelongsToUser(long experimentId, String username) {
        Experiment exp = experiments.get(experimentId);
        if (exp == null) return false;
        return exp.getOwnerUsername().equals(username);
    }
}