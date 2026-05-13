package ru.laba5.cli;

import ru.laba5.users.AuthService;

import java.util.List;

public class LogoutCommand implements Command {
    private final AuthService authService;

    public LogoutCommand(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void execute(List<String> args) {
        if (authService.isAuthenticated()) {
            authService.logout();
        } else {
            System.out.println("Вы не авторизованы. Войти можно командой 'login'");
        }
    }
}