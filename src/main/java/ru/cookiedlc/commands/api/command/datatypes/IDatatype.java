package ru.cookiedlc.commands.api.command.datatypes;

import ru.cookiedlc.commands.api.command.exception.CommandException;
import ru.cookiedlc.common.QuickImports;

import java.util.stream.Stream;

public interface IDatatype extends QuickImports {
    Stream<String> tabComplete(IDatatypeContext ctx) throws CommandException;
}
