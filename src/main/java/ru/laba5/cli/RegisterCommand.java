package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.users.AuthService;

import java.util.List;

public class RegisterCommand extends BaseCommand {

    public RegisterCommand(AuthService authService, InputReader reader) {
        super(authService, reader);
    }

    @Override
    public void execute(List<String> args) {
        while (true) {
            String login = reader.readNonEmpty("Логин: ");

            if (authService.isLoginTaken(login)) {
                System.out.println("Ошибка: пользователь с логином '" + login + "' уже существует");
                continue;
            }

            String password = reader.readNonEmpty("Пароль: ");

            if (authService.register(login, password)) {
                System.out.println("OK Регистрация успешна!");
                return;
            }
        }
    }
}