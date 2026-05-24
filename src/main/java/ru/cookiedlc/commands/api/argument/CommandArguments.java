
package ru.cookiedlc.commands.api.argument;

import ru.cookiedlc.commands.api.command.argument.ICommandArgument;
import ru.cookiedlc.commands.api.command.exception.CommandInvalidArgumentException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandArguments {

    private CommandArguments() {}

    private static final Pattern ARG_PATTERN = Pattern.compile("\\S+");

    public static List<ICommandArgument> from(String string, boolean preserveEmptyLast) {
        List<ICommandArgument> args = new ArrayList<>();
        Matcher argMatcher = ARG_PATTERN.matcher(string);
        int lastEnd = -1;
        while (argMatcher.find()) {
            args.add(new CommandArgument(
                    args.size(),
                    argMatcher.group(),
                    string.substring(argMatcher.start())
            ));
            lastEnd = argMatcher.end();
        }
        if (preserveEmptyLast && lastEnd < string.length()) {
            args.add(new CommandArgument(args.size(), "", ""));
        }
        return args;
    }

    public static List<ICommandArgument> from(String string) {
        return from(string, false);
    }

    public static CommandArgument unknown() {
        return new CommandArgument(-1, "<unknown>", "");
    }
}
