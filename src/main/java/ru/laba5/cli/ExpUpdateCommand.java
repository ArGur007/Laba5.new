package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.domain.Experiment;
import ru.laba5.service.ExperimentManager;

import java.util.List;

public class ExpUpdateCommand implements Command {
    private final ExperimentManager experimentManager;
    private final InputReader reader;
    private final String currentUser;

    public ExpUpdateCommand(ExperimentManager experimentManager, InputReader reader, String currentUser) {
        this.experimentManager = experimentManager;
        this.reader = reader;
        this.currentUser = currentUser;
    }

    @Override
    public void execute(List<String> args) {
        long id = reader.readLong("ID эксперимента: ");
        Experiment exp = experimentManager.findById(id);
        if (exp == null) {
            System.out.println("Эксперимент #" + id + " не найден");
            return;
        }

        String newName = reader.readString("Новое name [" + exp.getName() + "]: ");
        String newDesc = reader.readString("Новое desc [" + exp.getDescription() + "]: ");

        Experiment updated = exp;
        if (!newName.isEmpty()) {
            updated = updated.updateName(newName);
        }
        if (!newDesc.isEmpty()) {
            updated = updated.updateDescription(newDesc);
        }

        if (updated != exp) {
            experimentManager.update(updated);
            System.out.println("OK");
        } else {
            System.out.println("Ничего не изменено");
        }
    }
}