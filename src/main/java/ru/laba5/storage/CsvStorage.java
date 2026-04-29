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
import java.util.stream.Collectors;

public class CsvStorage {

    private static final char SEPARATOR = ';';

    // ======================== СОХРАНЕНИЕ ========================

    /**
     * Сохраняет все данные в три CSV-файла:
     * <basePath>_experiments.csv, <basePath>_runs.csv, <basePath>_results.csv
     */
    public void save(Path basePath,
                     ExperimentManager experimentManager,
                     RunManager runManager,
                     RunResultManager resultManager) throws IOException {
        saveEntities(basePath, "experiments", experimentManager.getAll(), Experiment.class);
        saveEntities(basePath, "runs", runManager.getAll(), Run.class);
        saveEntities(basePath, "results", resultManager.getAll(), RunResult.class);
    }

    private <T> void saveEntities(Path basePath, String suffix, List<T> entities, Class<T> clazz) throws IOException {
        Path filePath = Paths.get(basePath.toString().replace(".csv", "_" + suffix + ".csv"));
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            StatefulBeanToCsv<T> beanToCsv = new StatefulBeanToCsvBuilder<T>(writer)
                    .withSeparator(SEPARATOR)
                    .build();
            beanToCsv.write(entities);
        } catch (Exception e) {
            throw new IOException("Ошибка сохранения файла " + filePath + ": " + e.getMessage(), e);
        }
    }

    // ======================== ЗАГРУЗКА ========================

    /**
     * Загружает данные из трёх CSV-файлов.
     * Возвращает DataContainer с временными коллекциями.
     * В случае любой ошибки (включая дубликаты ID, несуществующие ссылки) выбрасывает исключение.
     */
    public DataContainer load(Path basePath) throws IOException {
        Path expPath = Paths.get(basePath.toString().replace(".csv", "_experiments.csv"));
        Path runPath = Paths.get(basePath.toString().replace(".csv", "_runs.csv"));
        Path resPath = Paths.get(basePath.toString().replace(".csv", "_results.csv"));

        Map<Long, Experiment> tempExperiments = loadEntities(expPath, Experiment.class);
        Map<Long, Run> tempRuns = loadEntities(runPath, Run.class);
        Map<Long, RunResult> tempResults = loadEntities(resPath, RunResult.class);

        // Проверка ссылочной целостности
        for (Run run : tempRuns.values()) {
            if (!tempExperiments.containsKey(run.getExperimentId())) {
                throw new IllegalArgumentException(
                        "Run id=" + run.getId() + " ссылается на несуществующий Experiment id=" + run.getExperimentId());
            }
        }
        for (RunResult result : tempResults.values()) {
            if (!tempRuns.containsKey(result.getRunId())) {
                throw new IllegalArgumentException(
                        "RunResult id=" + result.getId() + " ссылается на несуществующий Run id=" + result.getRunId());
            }
        }

        return new DataContainer(tempExperiments, tempRuns, tempResults);
    }

    private <T> Map<Long, T> loadEntities(Path path, Class<T> clazz) throws IOException {
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

            // Преобразуем список в Map по ID (предполагается, что у класса есть метод getId)
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
                        throw new IllegalArgumentException("Дубликат ID в файле: " + path);
                    }
            ));
        } catch (Exception e) {
            throw new IOException("Ошибка чтения файла " + path + ": " + e.getMessage(), e);
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
    }
}