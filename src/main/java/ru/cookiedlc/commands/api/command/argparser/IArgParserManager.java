
package ru.cookiedlc.commands.api.command.argparser;

import ru.cookiedlc.commands.api.command.argument.ICommandArgument;
import ru.cookiedlc.commands.api.command.exception.CommandInvalidTypeException;
import ru.cookiedlc.commands.api.command.registry.Registry;

public interface  IArgParserManager {

    <T> IArgParser.Stateless<T> getParserStateless(Class<T> type);

    <T, S> IArgParser.Stated<T, S> getParserStated(Class<T> type, Class<S> stateKlass);

    <T> T parseStateless(Class<T> type, ICommandArgument arg) throws CommandInvalidTypeException;

    <T, S> T parseStated(Class<T> type, Class<S> stateKlass, ICommandArgument arg, S state) throws CommandInvalidTypeException;

    Registry<IArgParser> getRegistry();
}
