package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.MeasurementParam;
import ru.laba5.domain.Run;
import ru.laba5.domain.RunResult;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

import java.util.List;

public class ResultAddCommand implements Command {
    private final RunManager runManager;
    private final RunResultManager resultManager;
    private final InputReader reader;
    private final String currentUser;

    public ResultAddCommand(RunManager runManager, RunResultManager resultManager, InputReader reader, String currentUser) {
        this.runManager = runManager;
        this.resultManager = resultManager;
        this.reader = reader;
        this.currentUser = currentUser;
    }

    @Override
    public void execute(List<String> args) {
        long runId;
        while (true) {
            runId = reader.readLong("run_id: ");
            Run run = runManager.findById(runId);
            if (run != null) break;
            System.out.println("Запуск #" + runId + " не найден");
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
        RunResult result = new RunResult(resultId, runId, param, value, unit, comment, currentUser);
        resultManager.add(result);
        System.out.println("OK result_id=" + resultId);
    }
}