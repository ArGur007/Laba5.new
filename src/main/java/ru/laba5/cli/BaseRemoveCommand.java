package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.users.AuthService;

public abstract class BaseRemoveCommand<T> extends BaseCommand {

    public BaseRemoveCommand(AuthService authService, InputReader reader) {
        super(authService, reader);
    }

    protected abstract T findById(long id);
    protected abstract void remove(long id, String currentUser);
    protected abstract boolean checkOwnership(long id, String currentUser);
    protected abstract String getEntityName();
    protected abstract String getOwnerName(long id);

    @Override
    public void execute(java.util.List<String> args) {
        if (!requireAuth()) return;

        long id = reader.readLong("ID " + getEntityName() + " для удаления: ");

        T entity = findById(id);
        if (entity == null) {
            printNotFound(getEntityName(), id);
            return;
        }

        if (!checkOwnership(id, getCurrentUser())) {
            String ownerName = getOwnerName(id);
            System.out.println("Ошибка: у вас нет прав на удаление этого " + getEntityName());
            System.out.println("Владелец: " + ownerName);
            System.out.println("Ваш логин: " + getCurrentUser());
            return;
        }

        remove(id, getCurrentUser());
        System.out.println("OK " + getEntityName() + " #" + id + " удалён");
    }
}