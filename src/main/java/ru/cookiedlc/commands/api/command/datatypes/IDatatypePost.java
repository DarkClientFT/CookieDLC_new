package ru.cookiedlc.commands.api.command.datatypes;

import ru.cookiedlc.commands.api.command.exception.CommandException;

public interface IDatatypePost<T, O> extends IDatatype {
    T apply(IDatatypeContext datatypeContext, O original) throws CommandException;
}
