
package ru.cookiedlc.commands.api.argparser;

import ru.cookiedlc.commands.api.command.argparser.IArgParser;
import ru.cookiedlc.commands.api.command.argparser.IArgParserManager;
import ru.cookiedlc.commands.api.command.argument.ICommandArgument;
import ru.cookiedlc.commands.api.command.exception.CommandInvalidTypeException;
import ru.cookiedlc.commands.api.command.exception.CommandNoParserForTypeException;
import ru.cookiedlc.commands.api.command.registry.Registry;

public enum ArgParserManager implements IArgParserManager {
    INSTANCE;

    public final Registry<IArgParser> registry = new Registry<>();

    ArgParserManager() {
        DefaultArgParsers.ALL.forEach(this.registry::register);
    }

    @Override
    public <T> IArgParser.Stateless<T> getParserStateless(Class<T> type) {
        return this.registry.descendingStream()
                .filter(IArgParser.Stateless.class::isInstance)
                .map(IArgParser.Stateless.class::cast)
                .filter(parser -> parser.getTarget().isAssignableFrom(type))
                .findFirst()
                .orElse(null);
    }

    @Override
    public <T, S> IArgParser.Stated<T, S> getParserStated(Class<T> type, Class<S> stateKlass) {
        return this.registry.descendingStream()
                .filter(IArgParser.Stated.class::isInstance)
                .map(IArgParser.Stated.class::cast)
                .filter(parser -> parser.getTarget().isAssignableFrom(type))
                .filter(parser -> parser.getStateType().isAssignableFrom(stateKlass))
                .map(IArgParser.Stated.class::cast)
                .findFirst()
                .orElse(null);
    }

    @Override
    public <T> T parseStateless(Class<T> type, ICommandArgument arg) throws CommandInvalidTypeException {
        IArgParser.Stateless<T> parser = this.getParserStateless(type);
        if (parser == null) {
            throw new CommandNoParserForTypeException(type);
        }
        try {
            return parser.parseArg(arg);
        } catch (Exception exc) {
            throw new CommandInvalidTypeException(arg, type.getSimpleName());
        }
    }

    @Override
    public <T, S> T parseStated(Class<T> type, Class<S> stateKlass, ICommandArgument arg, S state) throws CommandInvalidTypeException {
        IArgParser.Stated<T, S> parser = this.getParserStated(type, stateKlass);
        if (parser == null) {
            throw new CommandNoParserForTypeException(type);
        }
        try {
            return parser.parseArg(arg, state);
        } catch (Exception exc) {
            throw new CommandInvalidTypeException(arg, type.getSimpleName());
        }
    }

    @Override
    public Registry<IArgParser> getRegistry() {
        return this.registry;
    }
}
