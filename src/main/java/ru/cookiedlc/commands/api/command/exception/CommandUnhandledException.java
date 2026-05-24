package ru.cookiedlc.commands.api.command.exception;

import ru.cookiedlc.commands.api.command.ICommand;
import ru.cookiedlc.commands.api.command.argument.ICommandArgument;
import ru.cookiedlc.common.QuickLogger;

import java.util.List;

public class CommandUnhandledException extends RuntimeException implements ICommandException, QuickLogger {

    public CommandUnhandledException(String message) {
        super(message);
    }

    public CommandUnhandledException(Throwable cause) {
        super(cause);
    }

    @Override
    public void handle(ICommand command, List<ICommandArgument> args) {
    }
}
