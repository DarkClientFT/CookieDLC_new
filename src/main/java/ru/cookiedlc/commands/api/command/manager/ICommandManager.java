package ru.cookiedlc.commands.api.command.manager;

import net.minecraft.util.Pair;
import ru.cookiedlc.commands.api.command.ICommand;
import ru.cookiedlc.commands.api.command.argument.ICommandArgument;
import ru.cookiedlc.commands.api.command.registry.Registry;

import java.util.List;
import java.util.stream.Stream;

public interface ICommandManager {
    Registry<ICommand> getRegistry();

    ICommand getCommand(String name);

    boolean execute(String string);

    boolean execute(Pair<String, List<ICommandArgument>> expanded);

    Stream<String> tabComplete(Pair<String, List<ICommandArgument>> expanded);

    Stream<String> tabComplete(String prefix);
}
