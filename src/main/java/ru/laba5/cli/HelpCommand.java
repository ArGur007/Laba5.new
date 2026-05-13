package ru.laba5.cli;

import java.util.List;

public class HelpCommand implements Command {
    @Override
    public void execute(List<String> args) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("   ДОСТУПНЫЕ КОМАНДЫ");
        System.out.println("=".repeat(50));
        System.out.println("\n--- АВТОРИЗАЦИЯ ---");
        System.out.println("register                    - регистрация нового пользователя");
        System.out.println("login                       - вход в систему");
        System.out.println("logout                      - выход из системы");
        System.out.println("whoami                      - показать текущего пользователя");
        System.out.println("\n--- ЭКСПЕРИМЕНТЫ ---");
        System.out.println("exp_create                  - создать эксперимент");
        System.out.println("exp_list [--mine]           - список экспериментов");
        System.out.println("exp_show                    - детали эксперимента");
        System.out.println("exp_update                  - обновить эксперимент (только свои)");
        System.out.println("\n--- ЗАПУСКИ ---");
        System.out.println("run_add                     - добавить запуск (только для своих экспериментов)");
        System.out.println("run_list                    - список запусков");
        System.out.println("run_show                    - детали запуска");
        System.out.println("\n--- РЕЗУЛЬТАТЫ ---");
        System.out.println("res_add                     - добавить результат (только для своих экспериментов)");
        System.out.println("res_list                    - список результатов");
        System.out.println("exp_summary                 - сводка по параметрам");
        System.out.println("\n--- ФАЙЛЫ ---");
        System.out.println("save                        - сохранить данные в CSV");
        System.out.println("load                        - загрузить данные из CSV");
        System.out.println("exp_remove                  - удалить эксперимент (только свои)");
        System.out.println("run_remove                  - удалить запуск (только свои)");
        System.out.println("res_remove                  - удалить результат (только свои)");
        System.out.println("\n--- ОБЩИЕ ---");
        System.out.println("help                        - показать справку");
        System.out.println("exit                        - выход");
        System.out.println("=".repeat(50));
    }
}