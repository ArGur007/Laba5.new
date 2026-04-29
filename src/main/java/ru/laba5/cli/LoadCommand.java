package ru.laba5.cli;

import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import ru.laba5.storage.CsvStorage;

import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LoadCommand implements Command {
    private final ExperimentManager experimentManager;
    private final RunManager runManager;
    private final RunResultManager resultManager;
    private final CsvStorage storage;

    public LoadCommand(ExperimentManager experimentManager,
                       RunManager runManager,
                       RunResultManager resultManager,
                       CsvStorage storage) {
        this.experimentManager = experimentManager;
        this.runManager = runManager;
        this.resultManager = resultManager;
        this.storage = storage;
    }

    @Override
    public void execute(List<String> args) {
        if (args.isEmpty()) {
            System.out.println("Ошибка: укажите путь к файлу. Пример: load data.csv");
            return;
        }
        Path path = Paths.get(args.get(0));
        try {
            CsvStorage.DataContainer data = storage.load(path);
            experimentManager.clear();
            runManager.clear();
            resultManager.clear();
            data.experiments.values().forEach(experimentManager::add);
            data.runs.values().forEach(runManager::add);
            data.results.values().forEach(resultManager::add);
            experimentManager.syncIdGenerator();
            runManager.syncIdGenerator();
            resultManager.syncIdGenerator();
            System.out.println("Данные загружены из " + path.toAbsolutePath());
        } catch (AccessDeniedException e) {
            System.out.println("Ошибка загрузки: нет доступа для чтения файла " + path);
        } catch (Exception e) {
            System.out.println("Ошибка загрузки: " + e.getMessage());
        }
    }
}