package ru.laba5.users;

import ru.laba5.db.dao.UserDAO;
import ru.laba5.users.User;
import java.sql.SQLException;

public class AuthService {
    private User currentUser = null;

    public boolean isLoginTaken(String login) {
        try { return UserDAO.exists(login); }
        catch (SQLException e) { e.printStackTrace(); return true; }
    }

    public boolean userExists(String login) { return isLoginTaken(login); }

    public boolean register(String login, String password) {
        if (login == null || login.trim().isEmpty()) return false;
        if (password == null || password.trim().isEmpty()) return false;
        if (login.length() > 64) return false;
        try {
            if (UserDAO.exists(login)) return false;
            String hash = PasswordHasher.hash(password);
            int id = UserDAO.create(login, hash);
            currentUser = new User(id, login, hash);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String login, String password) {
        if (login == null || login.trim().isEmpty()) return false;
        try {
            User user = UserDAO.findByLogin(login);
            if (user == null) return false;
            String hash = PasswordHasher.hash(password);
            if (user.getPasswordHash().equals(hash)) {
                currentUser = user;
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void logout() { currentUser = null; }
    public User getCurrentUser() { return currentUser; }
    public String getCurrentUsername() { return currentUser != null ? currentUser.getLogin() : null; }
    public int getCurrentUserId() { return currentUser != null ? currentUser.getId() : -1; }
    public boolean isAuthenticated() { return currentUser != null; }
}