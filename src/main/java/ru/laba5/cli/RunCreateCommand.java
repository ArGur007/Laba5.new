package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Experiment;
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

        Experiment experiment = experimentManager.findById(expId);
        if (experiment == null) {
            printNotFound("Эксперимент", expId);
            return;
        }

        if (!experiment.getOwnerUsername().equals(getCurrentUser())) {
            handleError("У вас нет прав на добавление запусков к этому эксперименту");
            System.out.println("Владелец эксперимента: " + experiment.getOwnerUsername());
            return;
        }

        String name = reader.readNonEmpty("Название запуска: ");
        String operator = reader.readString("Оператор [" + getCurrentUser() + "]: ");
        if (operator.isEmpty()) operator = getCurrentUser();

        Run run = new Run( expId, name, operator);
        runManager.add(run);

        System.out.println("OK run_id=" + run.getId());
    }
}