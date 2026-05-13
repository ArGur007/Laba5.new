package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.users.AuthService;

public abstract class BaseCommand implements Command {
    protected final AuthService authService;
    protected final InputReader reader;

    public BaseCommand(AuthService authService, InputReader reader) {
        this.authService = authService;
        this.reader = reader;
    }

    protected boolean requireAuth() {
        if (!authService.isAuthenticated()) {
            System.out.println("Ошибка: нужно войти в систему (login)");
            return false;
        }
        return true;
    }

    protected String getCurrentUser() {
        return authService.getCurrentUsername();
    }

    protected void handleError(String message) {
        System.out.println("Ошибка: " + message);
    }

    protected boolean checkOwnership(String owner, String objectType, long id) {
        if (!owner.equals(getCurrentUser())) {
            System.out.println("Ошибка: у вас нет прав на " + objectType + " #" + id);
            System.out.println("Владелец: " + owner);
            return false;
        }
        return true;
    }

    protected void printNotFound(String objectType, long id) {
        System.out.println(objectType + " #" + id + " не найден");
    }
}