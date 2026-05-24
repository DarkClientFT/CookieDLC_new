package ru.cookiedlc.commands.api.command;

import ru.cookiedlc.commands.api.command.argparser.IArgParserManager;

public interface ICommandSystem {
    IArgParserManager getParserManager();
}
