package ru.laba5.storage;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import ru.laba5.domain.Experiment;
import ru.laba5.domain.Run;
import ru.laba5.domain.RunResult;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CsvStorage {
    private static final Logger LOGGER = Logger.getLogger(CsvStorage.class.getName());
    private static final char SEPARATOR = ';';

    // Константы с именами файлов
    public static final String EXPERIMENTS_FILE = "data_experiments.csv";
    public static final String RUNS_FILE = "data_runs.csv";
    public static final String RESULTS_FILE = "data_results.csv";

    // ======================== СОХРАНЕНИЕ ========================

    public void save(ExperimentManager experimentManager,
                     RunManager runManager,
                     RunResultManager resultManager) throws IOException {
        saveEntities(EXPERIMENTS_FILE, experimentManager.getAll(), Experiment.class);
        saveEntities(RUNS_FILE, runManager.getAll(), Run.class);
        saveEntities(RESULTS_FILE, resultManager.getAll(), RunResult.class);
        LOGGER.info("Данные сохранены");
    }

    private <T> void saveEntities(String filePath, List<T> entities, Class<T> clazz) throws IOException {
        Path path = Paths.get(filePath);
        try (Writer writer = Files.newBufferedWriter(path)) {
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer)
                    .withSeparator(SEPARATOR)
                    .build();
            beanToCsv.write(entities);
        } catch (Exception e) {
            throw new IOException("Ошибка сохранения файла " + filePath + ": " + e.getMessage(), e);
        }
    }

    // ======================== ЗАГРУЗКА ========================

    public DataContainer load() throws IOException {
        Map<Long, Experiment> tempExperiments = loadEntities(EXPERIMENTS_FILE, Experiment.class);
        Map<Long, Run> tempRuns = loadEntities(RUNS_FILE, Run.class);
        Map<Long, RunResult> tempResults = loadEntities(RESULTS_FILE, RunResult.class);

        validateReferences(tempExperiments, tempRuns, tempResults);

        return new DataContainer(tempExperiments, tempRuns, tempResults);
    }

    public boolean hasDataFiles() {
        return Files.exists(Paths.get(EXPERIMENTS_FILE));
    }

    private void validateReferences(Map<Long, Experiment> experiments,
                                    Map<Long, Run> runs,
                                    Map<Long, RunResult> results) {
        for (Run run : runs.values()) {
            if (!experiments.containsKey(run.getExperimentId())) {
                throw new IllegalArgumentException(
                        "Run id=" + run.getId() + " ссылается на несуществующий Experiment id=" + run.getExperimentId());
            }
        }
        for (RunResult result : results.values()) {
            if (!runs.containsKey(result.getRunId())) {
                throw new IllegalArgumentException(
                        "RunResult id=" + result.getId() + " ссылается на несуществующий Run id=" + result.getRunId());
            }
        }
    }

    private <T> Map<Long, T> loadEntities(String filePath, Class<T> clazz) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return new HashMap<>();
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            List<T> list = new CsvToBeanBuilder<T>(reader)
                    .withSeparator(SEPARATOR)
                    .withType(clazz)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();

            return list.stream().collect(Collectors.toMap(
                    entity -> {
                        try {
                            return (long) entity.getClass().getMethod("getId").invoke(entity);
                        } catch (Exception e) {
                            throw new RuntimeException("Не удалось получить ID для объекта " + entity, e);
                        }
                    },
                    entity -> entity,
                    (existing, replacement) -> {
                        throw new IllegalArgumentException("Дубликат ID в файле: " + filePath);
                    }
            ));
        }
    }

    // ======================== DataContainer ========================
    public static class DataContainer {
        public final Map<Long, Experiment> experiments;
        public final Map<Long, Run> runs;
        public final Map<Long, RunResult> results;

        public DataContainer(Map<Long, Experiment> experiments,
                             Map<Long, Run> runs,
                             Map<Long, RunResult> results) {
            this.experiments = experiments;
            this.runs = runs;
            this.results = results;
        }

        public boolean isEmpty() {
            return experiments.isEmpty() && runs.isEmpty() && results.isEmpty();
        }
    }
}