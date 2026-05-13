package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.MeasurementParam;
import ru.laba5.domain.Run;
import ru.laba5.domain.RunResult;
import ru.laba5.users.AuthService;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

import java.util.List;

public class ResultAddCommand extends BaseCommand {
    private final RunManager runManager;
    private final RunResultManager resultManager;

    public ResultAddCommand(RunManager runManager, RunResultManager resultManager,
                            AuthService authService, InputReader reader) {
        super(authService, reader);
        this.runManager = runManager;
        this.resultManager = resultManager;
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

        if (!runManager.isRunBelongsToUser(runId, getCurrentUser())) {
            handleError("У вас нет прав на добавление результатов к этому запуску");
            long expId = run.getExperimentId();
            System.out.println("Владелец эксперимента: " + runManager.getExperimentOwner(expId));
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

        long resultId = resultManager.getNextId();
        RunResult result = new RunResult(resultId, runId, param, value, unit, comment, getCurrentUser());
        resultManager.add(result);
        System.out.println("OK result_id=" + resultId);
    }
}