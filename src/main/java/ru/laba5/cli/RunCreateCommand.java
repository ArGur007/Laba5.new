package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Run;
import ru.laba5.users.AuthService;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;

import java.util.List;

public class RunCreateCommand extends BaseCommand {
    private final ExperimentManager experimentManager;
    private final RunManager runManager;

    public RunCreateCommand(ExperimentManager experimentManager, RunManager runManager,
                            AuthService authService, InputReader reader) {
        super(authService, reader);
        this.experimentManager = experimentManager;
        this.runManager = runManager;
    }

    @Override
    public void execute(List<String> args) {
        if (!requireAuth()) return;

        long expId = reader.readLong("ID эксперимента: ");

        if (experimentManager.findById(expId) == null) {
            printNotFound("Эксперимент", expId);
            return;
        }

        if (!experimentManager.isExperimentBelongsToUser(expId, getCurrentUser())) {
            handleError("У вас нет прав на добавление запусков к этому эксперименту");
            System.out.println("Владелец эксперимента: " + experimentManager.findById(expId).getOwnerUsername());
            return;
        }

        String name = reader.readNonEmpty("Название запуска: ");
        String operator = reader.readString("Оператор [" + getCurrentUser() + "]: ");
        if (operator.isEmpty()) operator = getCurrentUser();

        long runId = runManager.getNextId();
        Run run = new Run(runId, expId, name, operator);
        runManager.add(run);
        System.out.println("OK run_id=" + runId);
    }
}