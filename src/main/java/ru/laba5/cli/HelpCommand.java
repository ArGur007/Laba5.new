package ru.laba5.cli;

import java.util.List;

public class HelpCommand implements Command {
    @Override
    public void execute(List<String> args) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("   ДОСТУПНЫЕ КОМАНДЫ");
        System.out.println("=".repeat(50));
        System.out.println("help                        - показать справку");
        System.out.println("exp_create                  - создать эксперимент");
        System.out.println("exp_list [--mine]           - список экспериментов");
        System.out.println("exp_show <id>               - детали эксперимента");
        System.out.println("exp_update <id>             - обновить эксперимент");
        System.out.println("run_add <experiment_id>     - добавить запуск");
        System.out.println("run_list <experiment_id>    - список запусков");
        System.out.println("run_show <run_id>           - детали запуска");
        System.out.println("res_add <run_id>            - добавить результат");
        System.out.println("res_list <run_id>           - список результатов");
        System.out.println("exp_summary <experiment_id> - сводка по параметрам");
        System.out.println("exit                         - выход");
        System.out.println("save <path>                 - сохранить данные в CSV файл");
        System.out.println("load <path>                 - загрузить данные из CSV файла");
        System.out.println("=".repeat(50));
    }
}
