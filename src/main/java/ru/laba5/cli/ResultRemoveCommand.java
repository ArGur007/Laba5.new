package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.RunResult;
import ru.laba5.users.AuthService;
import ru.laba5.service.RunManager;
import ru.laba5.service.RunResultManager;

public class ResultRemoveCommand extends BaseRemoveCommand<RunResult> {
    private final RunResultManager resultManager;
    private final RunManager runManager;

    public ResultRemoveCommand(RunResultManager resultManager, RunManager runManager,
                               AuthService authService, InputReader reader) {
        super(authService, reader);
        this.resultManager = resultManager;
        this.runManager = runManager;
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
        return runManager.isRunBelongsToUser(result.getRunId(), currentUser);
    }

    @Override
    protected String getEntityName() {
        return "результат";
    }

    @Override
    protected String getOwnerName(long id) {
        RunResult result = resultManager.findById(id);
        if (result == null) return "неизвестен";
        return runManager.getExperimentOwnerByRunId(result.getRunId());
    }
}