package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Experiment;
import ru.laba5.domain.Run;
import ru.laba5.users.AuthService;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;

public class RunRemoveCommand extends BaseRemoveCommand<Run> {
    private final RunManager runManager;
    private final ExperimentManager experimentManager;

    public RunRemoveCommand(RunManager runManager, ExperimentManager experimentManager,
                            AuthService authService, InputReader reader) {
        super(authService, reader);
        this.runManager = runManager;
        this.experimentManager = experimentManager;
    }

    @Override
    protected Run findById(long id) {
        return runManager.findById(id);
    }

    @Override
    protected void remove(long id, String currentUser) {
        runManager.remove(id);
    }

    @Override
    protected boolean checkOwnership(long id, String currentUser) {
        Run run = runManager.findById(id);
        if (run == null) return false;
        Experiment exp = experimentManager.findById(run.getExperimentId());
        return exp != null && exp.getOwnerUsername().equals(currentUser);
    }

    @Override
    protected String getEntityName() {
        return "запуска";
    }

    @Override
    protected String getOwnerName(long id) {
        Run run = runManager.findById(id);
        if (run == null) return "неизвестен";
        Experiment exp = experimentManager.findById(run.getExperimentId());
        return exp != null ? exp.getOwnerUsername() : "неизвестен";
    }
}