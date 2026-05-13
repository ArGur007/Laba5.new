package ru.laba5.users;

import ru.laba5.domain.User;
import ru.laba5.users.UserStorage;
import ru.laba5.users.PasswordHasher;

public class AuthService {
    private final UserStorage userStorage = new UserStorage();
    private User currentUser = null;

    public boolean isLoginTaken(String login) {
        return userStorage.exists(login);
    }

    public boolean userExists(String login) {
        return userStorage.exists(login);
    }

    public boolean register(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        if (login.length() > 64) {
            return false;
        }
        if (userStorage.exists(login)) {
            return false;
        }

        String hash = PasswordHasher.hash(password);
        User user = new User(login, hash);
        return userStorage.addUser(user);
    }

    public boolean login(String login, String password) {
        if (login == null || login.trim().isEmpty()) {
            System.out.println("Ошибка: введите логин");
            return false;
        }
        User user = userStorage.findByLogin(login);
        if (user == null) {
            System.out.println("Ошибка: пользователь не найден");
            return false;
        }

        String hash = PasswordHasher.hash(password);
        if (user.getPasswordHash().equals(hash)) {
            currentUser = user;
            System.out.println("Добро пожаловать, " + login + "!");
            return true;
        } else {
            System.out.println("Ошибка: неверный пароль");
            return false;
        }
    }

    public void logout() {
        if (currentUser != null) {
            System.out.println("До свидания, " + currentUser.getLogin() + "!");
            currentUser = null;
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getLogin() : null;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }
}