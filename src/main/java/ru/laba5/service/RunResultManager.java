package ru.laba5.service;

import ru.laba5.db.dao.RunResultDAO;
import ru.laba5.domain.Experiment;
import ru.laba5.domain.MeasurementParam;
import ru.laba5.domain.Run;
import ru.laba5.domain.RunResult;
import ru.laba5.users.AuthService;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RunResultManager {
    private final Map<Long, RunResult> cache = new HashMap<>();
    private final AuthService authService;
    private final RunManager runManager;
    private final ExperimentManager experimentManager;

    public RunResultManager(AuthService authService, RunManager runManager, ExperimentManager experimentManager) {
        this.authService = authService;
        this.runManager = runManager;
        this.experimentManager = experimentManager;
        reload();
    }

    public void reload() {
        try {
            List<RunResult> list = RunResultDAO.loadAll();
            cache.clear();
            list.forEach(r -> cache.put(r.getId(), r));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки результатов", e);
        }
    }

    private int currentUserId() {
        return authService.getCurrentUserId();
    }

    private String currentUserLogin() {
        return authService.getCurrentUsername();
    }

    public List<RunResult> getAll() {
        return new ArrayList<>(cache.values());
    }

    // Возвращает null, если не найдено
    public RunResult findById(long id) {
        return cache.get(id);
    }

    public List<RunResult> getByRun(long runId) {
        return cache.values().stream()
                .filter(r -> r.getRunId() == runId)
                .toList();
    }

    public void add(RunResult result) {
        requireRunOwnership(result.getRunId());
        int ownerId = currentUserId();
        try {
            int newId = RunResultDAO.create(result, ownerId);
            result.setId(newId);
            result.setOwnerUsername(currentUserLogin());
            cache.put(result.getId(), result);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления результата", e);
        }
    }

    public void update(RunResult result) {
        requireResultOwnership(result.getId());
        int ownerId = currentUserId();
        try {
            if (!RunResultDAO.update(result, ownerId))
                throw new SecurityException("Результат не найден или не принадлежит вам");
            cache.put(result.getId(), result);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления", e);
        }
    }

    public void remove(long id) {
        requireResultOwnership(id);
        int ownerId = currentUserId();
        try {
            if (!RunResultDAO.delete(id, ownerId))
                throw new SecurityException("Результат не найден");
            cache.remove(id);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления", e);
        }
    }

    public void clear() {
        int ownerId = currentUserId();
        try {
            RunResultDAO.deleteByOwner(ownerId);
            cache.values().removeIf(result -> {
                Run run = runManager.findById(result.getRunId());
                if (run == null) return true;
                Experiment exp = experimentManager.findById(run.getExperimentId());
                return exp == null || !exp.getOwnerUsername().equals(currentUserLogin());
            });
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка очистки", e);
        }
    }

    private void requireRunOwnership(long runId) {
        Run run = runManager.findById(runId);
        if (run == null) throw new IllegalArgumentException("Запуск #" + runId + " не найден");
        Experiment exp = experimentManager.findById(run.getExperimentId());
        if (exp == null || !exp.getOwnerUsername().equals(currentUserLogin()))
            throw new SecurityException("У вас нет прав на этот запуск");
    }

    private void requireResultOwnership(long resultId) {
        RunResult result = cache.get(resultId);
        if (result == null) throw new IllegalArgumentException("Результат #" + resultId + " не найден");
        requireRunOwnership(result.getRunId());
    }

    public Map<MeasurementParam, Summary> getSummaryByExperiment(long experimentId) {
        List<RunResult> expResults = runManager.getByExperiment(experimentId).stream()
                .flatMap(run -> getByRun(run.getId()).stream())
                .collect(Collectors.toList());
        return expResults.stream()
                .collect(Collectors.groupingBy(
                        RunResult::getParam,
                        Collectors.collectingAndThen(
                                Collectors.mapping(RunResult::getValue, Collectors.toList()),
                                list -> {
                                    DoubleSummaryStatistics stats = list.stream().mapToDouble(Double::doubleValue).summaryStatistics();
                                    return new Summary(stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());
                                }
                        )
                ));
    }

    public static class Summary {
        private final long count;
        private final double min, max, avg;
        public Summary(long count, double min, double max, double avg) {
            this.count = count; this.min = min; this.max = max; this.avg = avg;
        }
        public long getCount() { return count; }
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getAvg() { return avg; }
        @Override public String toString() {
            return String.format("count=%d min=%.2f max=%.2f avg=%.2f", count, min, max, avg);
        }
    }
}