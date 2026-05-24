package ru.cookiedlc.commands;

import ru.cookiedlc.commands.api.command.ICommandSystem;
import ru.cookiedlc.commands.api.command.argparser.IArgParserManager;
import ru.cookiedlc.commands.api.argparser.ArgParserManager;

public enum CommandSystem implements ICommandSystem {
    INSTANCE;

    @Override
    public IArgParserManager getParserManager() {
        return ArgParserManager.INSTANCE;
    }
}
