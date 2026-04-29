package ru.laba5.service;

import ru.laba5.domain.MeasurementParam;
import ru.laba5.domain.RunResult;

import java.util.*;
import java.util.stream.Collectors;

public class RunResultManager {
    private final Map<Long, RunResult> results = new HashMap<>();
    private final IdGenerator idGenerator = new IdGenerator();
    private final RunManager runManager;

    public RunResultManager(RunManager runManager) {
        this.runManager = runManager;
    }

    public long getNextId() {
        return idGenerator.nextId();
    }

    public void add(RunResult result) {
        if (!runManager.exists(result.getRunId())) {
            throw new IllegalArgumentException("Run #" + result.getRunId() + " does not exist");
        }
        if (results.containsKey(result.getId())) {
            throw new IllegalArgumentException("Result ID " + result.getId() + " already exists");
        }
        results.put(result.getId(), result);
    }

    public RunResult findById(long id) {
        return results.get(id);
    }

    public List<RunResult> getByRun(long runId) {
        return results.values().stream()
                .filter(r -> r.getRunId() == runId)
                .collect(Collectors.toList());
    }

    // Получить все результаты эксперимента (по id эксперимента)
    public List<RunResult> getByExperiment(long experimentId) {
        return runManager.getByExperiment(experimentId).stream()
                .flatMap(run -> getByRun(run.getId()).stream())
                .collect(Collectors.toList());
    }

    public List<RunResult> getAll() {
        return new ArrayList<>(results.values());
    }

    public void update(RunResult result) {
        if (!results.containsKey(result.getId())) {
            throw new IllegalArgumentException("Result #" + result.getId() + " not found");
        }
        results.put(result.getId(), result);
    }

    public void remove(long id) {
        results.remove(id);
    }

    // Получить сводку по эксперименту (min/max/avg по каждому параметру)
    public Map<MeasurementParam, Summary> getSummaryByExperiment(long experimentId) {
        List<RunResult> expResults = getByExperiment(experimentId);
        return expResults.stream()
                .collect(Collectors.groupingBy(
                        RunResult::getParam,
                        Collectors.collectingAndThen(
                                Collectors.mapping(RunResult::getValue, Collectors.toList()),
                                list -> {
                                    DoubleSummaryStatistics stats = list.stream()
                                            .mapToDouble(Double::doubleValue).summaryStatistics();
                                    return new Summary(stats.getCount(), stats.getMin(),
                                            stats.getMax(), stats.getAverage());
                                }
                        )
                ));
    }

    public static class Summary {
        private final long count;
        private final double min, max, avg;

        public Summary(long count, double min, double max, double avg) {
            this.count = count;
            this.min = min;
            this.max = max;
            this.avg = avg;
        }

        public long getCount() { return count; }
        public double getMin() { return min; }
        public double getMax() { return max; }
        public double getAvg() { return avg; }

        @Override
        public String toString() {
            return String.format("count=%d min=%.2f max=%.2f avg=%.2f", count, min, max, avg);
        }
    }
    public void clear() {
        results.clear();
    }

    public void syncIdGenerator() {
        long maxId = results.keySet().stream()
                .max(Long::compareTo)
                .orElse(0L);
        idGenerator.syncWithMaxId(maxId);
    }

}