package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Experiment;
import ru.laba5.users.AuthService;
import ru.laba5.service.ExperimentManager;

public class ExpRemoveCommand extends BaseRemoveCommand<Experiment> {
    private final ExperimentManager experimentManager;

    public ExpRemoveCommand(ExperimentManager experimentManager, AuthService authService, InputReader reader) {
        super(authService, reader);
        this.experimentManager = experimentManager;
    }

    @Override
    protected Experiment findById(long id) {
        return experimentManager.findById(id);
    }

    @Override
    protected void remove(long id, String currentUser) {
        // Новый менеджер не требует передавать currentUser, проверка прав внутри
        experimentManager.remove(id);
    }

    @Override
    protected boolean checkOwnership(long id, String currentUser) {
        Experiment exp = experimentManager.findById(id);
        return exp != null && exp.getOwnerUsername().equals(currentUser);
    }

    @Override
    protected String getEntityName() {
        return "эксперимента";
    }

    @Override
    protected String getOwnerName(long id) {
        Experiment exp = experimentManager.findById(id);
        return exp != null ? exp.getOwnerUsername() : "неизвестен";
    }
}