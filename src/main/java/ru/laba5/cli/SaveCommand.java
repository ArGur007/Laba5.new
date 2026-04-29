package ru.laba5.cli;

import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;
import ru.laba5.storage.CsvStorage;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SaveCommand implements Command {
    private final ExperimentManager experimentManager;
    private final RunManager runManager;
    private final RunResultManager resultManager;
    private final CsvStorage storage;

    public SaveCommand(ExperimentManager experimentManager,
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
            System.out.println("Ошибка: укажите путь к файлу. Пример: save data.csv");
            return;
        }
        Path path = Paths.get(args.get(0));
        try {
            storage.save(path, experimentManager, runManager, resultManager);
            System.out.println("Данные сохранены в " + path.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }
}