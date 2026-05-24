
package ru.cookiedlc.commands;

import net.minecraft.util.Pair;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.commands.api.command.argument.ICommandArgument;
import ru.cookiedlc.commands.api.command.exception.CommandNotEnoughArgumentsException;
import ru.cookiedlc.commands.api.command.exception.CommandNotFoundException;
import ru.cookiedlc.commands.api.command.helpers.TabCompleteHelper;
import ru.cookiedlc.commands.api.command.manager.ICommandManager;
import ru.cookiedlc.event.api.EventManager;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.chat.ChatEvent;
import ru.cookiedlc.event.events.chat.TabCompleteEvent;
import ru.cookiedlc.common.QuickLogger;
import ru.cookiedlc.commands.api.argument.ArgConsumer;
import ru.cookiedlc.commands.api.argument.CommandArguments;
import ru.cookiedlc.commands.api.manager.CommandRepository;
import java.util.List;
import java.util.stream.Stream;

import static ru.cookiedlc.commands.api.command.IBaritoneChatControl.FORCE_COMMAND_PREFIX;

public class CommandDispatcher implements QuickLogger {
    private final ICommandManager manager;
    public static String prefix = ".";

    public CommandDispatcher(EventManager eventManager) {
        this.manager = Main.getInstance().getCommandRepository();
        eventManager.register(this);
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        String msg = event.getMessage();

        boolean forceRun = msg.startsWith(FORCE_COMMAND_PREFIX);
        if ((msg.startsWith(prefix)) || forceRun) {
            event.cancel();
            String commandStr = msg.substring(forceRun ? FORCE_COMMAND_PREFIX.length() : prefix.length());
            if (!runCommand(commandStr) && !commandStr.trim().isEmpty()) {
                new CommandNotFoundException(CommandRepository.expand(commandStr).getLeft()).handle(null, null);
            }
        } else if (runCommand(msg)) {
            event.cancel();
        }
    }

    public boolean runCommand(String msg) {
        if (msg.isEmpty()) {
            return this.runCommand("help");
        }
        Pair<String, List<ICommandArgument>> pair = CommandRepository.expand(msg);
        String command = pair.getLeft();
        String rest = msg.substring(pair.getLeft().length());
        ArgConsumer argc = new ArgConsumer(this.manager, pair.getRight());


        return this.manager.execute(pair);
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String eventPrefix = event.prefix;
        if (!eventPrefix.startsWith(prefix)) {
            return;
        }

        String msg = eventPrefix.substring(prefix.length());
        List<ICommandArgument> args = CommandArguments.from(msg, true);
        Stream<String> stream = tabComplete(msg);
        if (args.size() == 1) {
            stream = stream.map(x -> prefix + x);
        }
        event.completions = stream.toArray(String[]::new);
    }

    public Stream<String> tabComplete(String msg) {
        try {
            List<ICommandArgument> args = CommandArguments.from(msg, true);
            ArgConsumer argc = new ArgConsumer(this.manager, args);
            if (argc.hasAtMost(2)) {
                if (argc.hasExactly(1)) {
                    return new TabCompleteHelper()
                            .addCommands(this.manager)
                            .filterPrefix(argc.getString())
                            .stream();
                }
            }
            return this.manager.tabComplete(msg);
        } catch (CommandNotEnoughArgumentsException ignored) {
            return Stream.empty();
        }
    }
}
