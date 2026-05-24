package ru.cookiedlc.commands.api.command;

import ru.cookiedlc.commands.api.command.argument.IArgConsumer;
import ru.cookiedlc.commands.api.command.exception.CommandException;
import ru.cookiedlc.common.QuickLogger;

import java.util.List;
import java.util.stream.Stream;

public interface ICommand extends QuickLogger {
    void execute(String label, IArgConsumer args) throws CommandException;

    Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException;

    String getShortDesc();

    List<String> getLongDesc();

    List<String> getNames();

    default boolean hiddenFromHelp() {
        return false;
    }
}
