package ru.laba5.service;

import ru.laba5.domain.Run;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RunManager {
    private final Map<Long, Run> runs = new HashMap<>();
    private final IdGenerator idGenerator = new IdGenerator();
    private final ExperimentManager experimentManager;

    public RunManager(ExperimentManager experimentManager) {
        this.experimentManager = experimentManager;
    }

    public long getNextId() {
        return idGenerator.nextId();
    }

    public void add(Run run) {
        if (!experimentManager.exists(run.getExperimentId())) {
            throw new IllegalArgumentException("Experiment #" + run.getExperimentId() + " does not exist");
        }
        if (runs.containsKey(run.getId())) {
            throw new IllegalArgumentException("Run ID " + run.getId() + " already exists");
        }
        runs.put(run.getId(), run);
    }

    public Run findById(long id) {
        return runs.get(id);
    }

    public List<Run> getByExperiment(long experimentId) {
        return runs.values().stream()
                .filter(r -> r.getExperimentId() == experimentId)
                .collect(Collectors.toList());
    }

    public List<Run> getAll() {
        return new ArrayList<>(runs.values());
    }

    public void update(Run run) {
        if (!runs.containsKey(run.getId())) {
            throw new IllegalArgumentException("Run #" + run.getId() + " not found");
        }
        runs.put(run.getId(), run);
    }

    public void remove(long id) {
        runs.remove(id);
    }

    public boolean exists(long id) {
        return runs.containsKey(id);
    }

    public void clear() {
        runs.clear();
    }

    public void syncIdGenerator() {
        long maxId = runs.keySet().stream()
                .max(Long::compareTo)
                .orElse(0L);
        idGenerator.syncWithMaxId(maxId);
    }

}