package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Experiment;
import ru.laba5.domain.Run;
import ru.laba5.domain.RunResult;
import ru.laba5.users.AuthService;
import ru.laba5.service.ExperimentManager;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

public class ResultRemoveCommand extends BaseRemoveCommand<RunResult> {
    private final RunResultManager resultManager;
    private final RunManager runManager;
    private final ExperimentManager experimentManager;

    // Добавили ExperimentManager в конструктор
    public ResultRemoveCommand(RunResultManager resultManager, RunManager runManager,
                               ExperimentManager experimentManager,
                               AuthService authService, InputReader reader) {
        super(authService, reader);
        this.resultManager = resultManager;
        this.runManager = runManager;
        this.experimentManager = experimentManager;
    }

    @Override
    protected RunResult findById(long id) {
        return resultManager.findById(id);
    }

    @Override
    protected void remove(long id, String currentUser) {
        resultManager.remove(id);
    }

    @Override
    protected boolean checkOwnership(long id, String currentUser) {
        RunResult result = resultManager.findById(id);
        if (result == null) return false;
        Run run = runManager.findById(result.getRunId());
        if (run == null) return false;
        Experiment exp = experimentManager.findById(run.getExperimentId());
        return exp != null && exp.getOwnerUsername().equals(currentUser);
    }

    @Override
    protected String getEntityName() {
        return "результат";
    }

    @Override
    protected String getOwnerName(long id) {
        RunResult result = resultManager.findById(id);
        if (result == null) return "неизвестен";
        Run run = runManager.findById(result.getRunId());
        if (run == null) return "неизвестен";
        Experiment exp = experimentManager.findById(run.getExperimentId());
        return exp != null ? exp.getOwnerUsername() : "неизвестен";
    }
}