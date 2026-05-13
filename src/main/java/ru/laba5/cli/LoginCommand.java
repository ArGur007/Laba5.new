package ru.laba5.cli;

import ru.laba5.Validation.InputReader;
import ru.laba5.users.AuthService;

import java.util.List;

public class LoginCommand extends BaseCommand {

    public LoginCommand(AuthService authService, InputReader reader) {
        super(authService, reader);
    }

    @Override
    public void execute(List<String> args) {
        if (authService.isAuthenticated()) {
            System.out.println("Вы уже вошли как " + authService.getCurrentUsername());
            System.out.println("Сначала выполните logout");
            return;
        }

        while (true) {
            String login = reader.readNonEmpty("Логин: ");

            if (!authService.userExists(login)) {
                System.out.println("Ошибка: пользователь не найден");
                continue;
            }

            String password = reader.readNonEmpty("Пароль: ");

            if (authService.login(login, password)) {
                return;
            }

            System.out.println("Неверный пароль. Попробуйте снова.");
        }
    }
}