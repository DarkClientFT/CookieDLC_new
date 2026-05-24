package ru.cookiedlc.commands.api.command.exception;

import net.minecraft.util.Formatting;
import ru.cookiedlc.commands.api.command.ICommand;
import ru.cookiedlc.commands.api.command.argument.ICommandArgument;
import ru.cookiedlc.common.QuickLogger;

import java.util.List;

public interface ICommandException extends QuickLogger {

    String getMessage();

    default void handle(ICommand command, List<ICommandArgument> args) {
        logDirect(
                this.getMessage(),
                Formatting.RED
        );
    }
}
