package ru.cookiedlc.commands.commands.other;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Formatting;
import ru.cookiedlc.commands.api.command.Command;
import ru.cookiedlc.commands.api.command.argument.IArgConsumer;
import ru.cookiedlc.commands.api.command.exception.CommandException;
import ru.cookiedlc.commands.api.command.helpers.TabCompleteHelper;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.commands.CommandDispatcher;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrefixCommand extends Command implements QuickImports {
    public PrefixCommand() {
        super("prefix");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        String arg = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "list";
        if (arg.equals("set")) {
            args.requireMin(1);
            logDirect("Установлен префикс '" + Formatting.RED + (CommandDispatcher.prefix = args.getString()) + Formatting.GRAY + "'", Formatting.GRAY);
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasAny()) {
            String arg = args.getString();
            if (arg.equalsIgnoreCase("set")) {
                return new TabCompleteHelper().sortAlphabetically().prepend("name").stream();
            } else {
                return new TabCompleteHelper().sortAlphabetically().prepend("set").filterPrefix(arg).stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Позволяет менять префикс команд в моде";
    }

    
    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "С помощью этой команды можно изменить префикс команд в моде",
                "",
                "Использование:",
                "> prefix set <name> - устанавливает префикс команд"
        );
    }
}
