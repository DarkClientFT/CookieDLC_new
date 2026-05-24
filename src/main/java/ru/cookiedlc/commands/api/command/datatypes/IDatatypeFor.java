package ru.cookiedlc.commands.api.command.datatypes;

import ru.cookiedlc.commands.api.command.exception.CommandException;

public interface IDatatypeFor<T> extends IDatatype  {
    T get(IDatatypeContext datatypeContext) throws CommandException;
}
