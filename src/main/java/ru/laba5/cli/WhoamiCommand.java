package ru.laba5.cli;

import ru.laba5.users.AuthService;

import java.util.List;

public class WhoamiCommand implements Command {
    private final AuthService authService;

    public WhoamiCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void execute(List<String> args) {
        if (authService.isAuthenticated()) {
            System.out.println("Текущий пользователь: " + authService.getCurrentUsername());
        } else {
            System.out.println("Не авторизован");
        }
    }
}