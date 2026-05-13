package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Run;
import ru.laba5.users.AuthService;
import ru.laba5.service.RunManager;

public class RunRemoveCommand extends BaseRemoveCommand<Run> {
    private final RunManager runManager;

    public RunRemoveCommand(RunManager runManager, AuthService authService, InputReader reader) {
        super(authService, reader);
        this.runManager = runManager;
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
        return runManager.isRunBelongsToUser(id, currentUser);
    }

    @Override
    protected String getEntityName() {
        return "запуска";
    }

    @Override
    protected String getOwnerName(long id) {
        return runManager.getExperimentOwnerByRunId(id);
    }
}