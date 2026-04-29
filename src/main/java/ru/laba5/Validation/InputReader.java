package ru.laba5.Validation;

import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

public class InputReader {
    protected final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public <T> T read(String prompt, Function<String, T> parser, Predicate<T> validator, String error) {
        while (true) {
            System.out.print(prompt);
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    System.out.println("Ошибка: поле не может быть пустым");
                    continue;
                }
                T value = parser.apply(input);
                if (validator.test(value)) return value;
                System.out.println("Ошибка: " + error);
            } catch (Exception e) {
                System.out.println("Ошибка: неверный формат");
            }
        }
    }

    public long readLong(String prompt) {
        return read(prompt, Long::parseLong, x -> true, "введите число");
    }

    public double readDouble(String prompt) {
        return read(prompt, Double::parseDouble, x -> true, "введите число");
    }

    public String readNonEmpty(String prompt) {
        return read(prompt, x -> x, x -> !x.isEmpty(), "не пустое");
    }

    public String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
