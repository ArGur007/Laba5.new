package ru.laba5.cli;

import java.util.List;

public interface Command {
    void execute(List<String> args);
}
