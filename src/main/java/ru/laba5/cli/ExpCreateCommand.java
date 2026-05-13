package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Experiment;
import ru.laba5.users.AuthService;
import ru.laba5.service.ExperimentManager;

import java.util.List;

public class ExpCreateCommand extends BaseCommand {
    private final ExperimentManager experimentManager;

    public ExpCreateCommand(ExperimentManager experimentManager, AuthService authService, InputReader reader) {
        super(authService, reader);
        this.experimentManager = experimentManager;
    }

    @Override
    public void execute(List<String> args) {
        if (!requireAuth()) return;

        String name = reader.readNonEmpty("Название: ");
        String description = reader.readString("Описание: ");

        long id = experimentManager.getNextId();
        Experiment exp = new Experiment(id, name, description, getCurrentUser());
        experimentManager.add(exp);
        System.out.println("OK exp_id=" + id);
    }
}