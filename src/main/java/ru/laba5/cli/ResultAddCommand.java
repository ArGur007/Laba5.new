package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Experiment;
import ru.laba5.domain.MeasurementParam;
import ru.laba5.domain.Run;
import ru.laba5.domain.RunResult;
import ru.laba5.users.AuthService;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

import java.util.List;

public class ResultAddCommand extends BaseCommand {
    private final RunManager runManager;
    private final RunResultManager resultManager;
    private final ExperimentManager experimentManager;

    public ResultAddCommand(RunManager runManager, RunResultManager resultManager,
                            AuthService authService, InputReader reader,
                            ExperimentManager experimentManager) {
        super(authService, reader);
        this.runManager = runManager;
        this.resultManager = resultManager;
        this.experimentManager = experimentManager;
    }

    @Override
    public void execute(List<String> args) {
        if (!requireAuth()) return;

        long runId = reader.readLong("ID запуска: ");
        Run run = runManager.findById(runId);

        if (run == null) {
            printNotFound("Запуск", runId);
            return;
        }

        Experiment experiment = experimentManager.findById(run.getExperimentId());
        if (experiment == null || !experiment.getOwnerUsername().equals(getCurrentUser())) {
            handleError("У вас нет прав на добавление результатов к этому запуску");
            if (experiment != null) {
                System.out.println("Владелец эксперимента: " + experiment.getOwnerUsername());
            }
            return;
        }

        MeasurementParam param;
        while (true) {
            String paramStr = reader.readNonEmpty("Параметр (PH, CONDUCTIVITY, NITRATE): ");
            try {
                param = MeasurementParam.valueOf(paramStr.toUpperCase());
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: только PH, CONDUCTIVITY, NITRATE");
            }
        }

        double value = reader.readDouble("Значение: ");
        String unit = reader.readNonEmpty("Единицы: ");
        String comment = reader.readString("Комментарий: ");

        // Создаём временный объект с ID=0, БД сгенерирует новый
        RunResult result = new RunResult(runId, param, value, unit, comment, getCurrentUser());
        resultManager.add(result);
        System.out.println("OK result_id=" + result.getId());
    }
}