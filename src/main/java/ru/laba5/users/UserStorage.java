package ru.laba5.users;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ru.laba5.domain.User;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UserStorage {
    private static final String USERS_FILE = "users.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Map<String, User> users = new HashMap<>();

    public UserStorage() {
        load();
    }

    public boolean addUser(User user) {
        if (users.containsKey(user.getLogin())) {
            return false;
        }
        users.put(user.getLogin(), user);
        save();
        return true;
    }

    public User findByLogin(String login) {
        return users.get(login);
    }

    public boolean exists(String login) {
        return users.containsKey(login);
    }

    public void load() {
        Path path = Paths.get(USERS_FILE);
        if (!Files.exists(path)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            Type type = new TypeToken<Map<String, User>>() {}.getType();
            Map<String, User> loaded = gson.fromJson(reader, type);
            if (loaded != null) {
                users = loaded;
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки пользователей: " + e.getMessage());
        }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(Paths.get(USERS_FILE))) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения пользователей: " + e.getMessage());
        }
    }
}