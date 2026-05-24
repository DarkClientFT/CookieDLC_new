package ru.cookiedlc.commands;

import ru.cookiedlc.commands.commands.file.AutoBuyCommand;
import ru.cookiedlc.commands.commands.file.ConfigCommand;
import ru.cookiedlc.commands.commands.file.FriendCommand;
import ru.cookiedlc.commands.commands.game.*;
import ru.cookiedlc.commands.commands.other.DebugCommand;
import ru.cookiedlc.commands.commands.other.IRCCommand;
import ru.cookiedlc.commands.commands.other.PrefixCommand;
import ru.cookiedlc.commands.commands.render.BoxESPCommand;
import ru.cookiedlc.commands.commands.render.WayCommand;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.commands.api.command.ICommand;

import java.util.*;

public final class CommandRegister {

    public static List<ICommand> createAll() {
        Main main = Main.getInstance();
        List<ICommand> commands = new ArrayList<>(Arrays.asList(
                new BoxESPCommand(main),
                new ConfigCommand(main),
                new MacroCommand(main),
                new HelpCommand(main),
                new BindCommand(main),
                new WayCommand(main),
                new RCTCommand(main),
                new FriendCommand(),
                new PrefixCommand(),
                new IRCCommand(),
                new DebugCommand(),
                new AutoBuyCommand(main)
        ));
        return Collections.unmodifiableList(commands);
    }
}