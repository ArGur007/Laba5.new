package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Run;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;

import java.util.List;

public class RunCreateCommand implements Command {
    private final ExperimentManager experimentManager;
    private final RunManager runManager;
    private final InputReader reader;
    private final String currentUser;

    public RunCreateCommand(ExperimentManager experimentManager, RunManager runManager, InputReader reader, String currentUser) {
        this.experimentManager = experimentManager;
        this.runManager = runManager;
        this.reader = reader;
        this.currentUser = currentUser;
    }

    @Override
    public void execute(List<String> args) {
        long expId;
        while (true) {
            expId = reader.readLong("experiment_id: ");
            if (experimentManager.findById(expId) != null) break;
            System.out.println("Эксперимент #" + expId + " не найден");
        }

        String name = reader.readNonEmpty("Run name: ");
        String operator = reader.readString("Operator [" + currentUser + "]: ");
        if (operator.isEmpty()) operator = currentUser;

        long runId = runManager.getNextId();
        Run run = new Run(runId, expId, name, operator);
        runManager.add(run);
        System.out.println("OK run_id=" + runId);
    }
}