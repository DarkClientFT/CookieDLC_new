
package ru.cookiedlc.commands.api.command.argument;

import ru.cookiedlc.commands.api.command.argparser.IArgParser;
import ru.cookiedlc.commands.api.command.exception.CommandInvalidTypeException;

public interface ICommandArgument {

    int getIndex();

    String getValue();

    String getRawRest();

    <E extends Enum<?>> E getEnum(Class<E> enumClass) throws CommandInvalidTypeException;

    <T> T getAs(Class<T> type) throws CommandInvalidTypeException;

    <T> boolean is(Class<T> type);

    <T, S> T getAs(Class<T> type, Class<S> stateType, S state) throws CommandInvalidTypeException;

    <T, S> boolean is(Class<T> type, Class<S> stateType, S state);
}
