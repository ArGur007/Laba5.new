package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Experiment;
import ru.laba5.users.AuthService;
import ru.laba5.service.ExperimentManager;
import java.util.List;

public class ExpUpdateCommand extends BaseCommand {
    private final ExperimentManager experimentManager;

    public ExpUpdateCommand(ExperimentManager experimentManager, AuthService authService, InputReader reader) {
        super(authService, reader);
        this.experimentManager = experimentManager;
    }

    @Override
    public void execute(List<String> args) {
        if (!requireAuth()) return;

        long id = reader.readLong("ID эксперимента: ");
        Experiment exp = experimentManager.findById(id);

        if (exp == null) {
            printNotFound("Эксперимент", id);
            return;
        }

        if (!checkOwnership(exp.getOwnerUsername(), "эксперимент", id)) {
            return;
        }

        String newName = reader.readString("Новое название [" + exp.getName() + "]: ");
        String newDesc = reader.readString("Новое описание [" + exp.getDescription() + "]: ");

        Experiment updated = exp;
        if (!newName.isEmpty()) {
            updated = updated.updateName(newName);
        }
        if (!newDesc.isEmpty()) {
            updated = updated.updateDescription(newDesc);
        }

        if (updated != exp) {
            experimentManager.update(updated, getCurrentUser());
            System.out.println("OK");
        } else {
            System.out.println("Ничего не изменено");
        }
    }
}