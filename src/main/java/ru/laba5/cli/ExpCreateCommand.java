package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Experiment;
import ru.laba5.service.ExperimentManager;

import java.util.List;

public class ExpCreateCommand implements Command {
    private final ExperimentManager experimentManager;
    private final InputReader reader;
    private final String currentUser;

    public ExpCreateCommand(ExperimentManager experimentManager, InputReader reader, String currentUser) {
        this.experimentManager = experimentManager;
        this.reader = reader;
        this.currentUser = currentUser;
    }

    @Override
    public void execute(List<String> args) {
        String name = reader.readNonEmpty("Название: ");
        String description = reader.readString("Описание: ");

        long id = experimentManager.getNextId();
        Experiment exp = new Experiment(id, name, description, currentUser);
        experimentManager.add(exp);
        System.out.println("OK exp_id=" + id);
    }
}