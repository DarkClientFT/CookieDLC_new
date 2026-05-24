package ru.cookiedlc.commands.api.command.exception;

import ru.cookiedlc.commands.api.command.ICommand;
import ru.cookiedlc.commands.api.command.argument.ICommandArgument;
import ru.cookiedlc.common.QuickLogger;

import java.util.List;

public class CommandNotFoundException extends CommandException implements QuickLogger {

    public final String command;

    public CommandNotFoundException(String command) {
        super(String.format("Команда не найдена: %s", command));
        this.command = command;
    }

    @Override
    public void handle(ICommand command, List<ICommandArgument> args) {
       logDirect(getMessage());
    }
}
