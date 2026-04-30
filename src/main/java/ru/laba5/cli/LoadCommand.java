package ru.laba5.cli;

import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import ru.laba5.storage.CsvStorage;

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
        try {
            CsvStorage.DataContainer data = storage.load();

            experimentManager.clear();
            runManager.clear();
            resultManager.clear();

            data.experiments.values().forEach(experimentManager::add);
            data.runs.values().forEach(runManager::add);
            data.results.values().forEach(resultManager::add);

            experimentManager.syncIdGenerator();
            runManager.syncIdGenerator();
            resultManager.syncIdGenerator();

            System.out.println("Данные загружены из файлов:");
            System.out.println("Экспериментов: " + data.experiments.size());
            System.out.println("Запусков: " + data.runs.size());
            System.out.println("Результатов: " + data.results.size());

        } catch (Exception e) {
            System.out.println("Ошибка загрузки: " + e.getMessage());
        }
    }
}