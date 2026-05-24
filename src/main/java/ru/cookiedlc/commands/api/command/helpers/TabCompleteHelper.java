
package ru.cookiedlc.commands.api.command.helpers;


import net.minecraft.util.Identifier;
import ru.cookiedlc.commands.api.command.argument.IArgConsumer;
import ru.cookiedlc.commands.api.command.manager.ICommandManager;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TabCompleteHelper {

    private Stream<String> stream;

    public TabCompleteHelper(String[] base) {
        stream = Stream.of(base);
    }

    public TabCompleteHelper(List<String> base) {
        stream = base.stream();
    }

    public TabCompleteHelper() {
        stream = Stream.empty();
    }

    public TabCompleteHelper append(Stream<String> source) {
        stream = Stream.concat(stream, source);
        return this;
    }

    public TabCompleteHelper append(String... source) {
        return append(Stream.of(source));
    }

    public TabCompleteHelper append(Class<? extends Enum<?>> num) {
        return append(
                Stream.of(num.getEnumConstants())
                        .map(Enum::name)
                        .map(String::toLowerCase)
        );
    }

    public TabCompleteHelper prepend(Stream<String> source) {
        stream = Stream.concat(source, stream);
        return this;
    }

    public TabCompleteHelper prepend(String... source) {
        return prepend(Stream.of(source));
    }

    public TabCompleteHelper prepend(Class<? extends Enum<?>> num) {
        return prepend(
                Stream.of(num.getEnumConstants())
                        .map(Enum::name)
                        .map(String::toLowerCase)
        );
    }

    public TabCompleteHelper map(Function<String, String> transform) {
        stream = stream.map(transform);
        return this;
    }

    public TabCompleteHelper filter(Predicate<String> filter) {
        stream = stream.filter(filter);
        return this;
    }

    public TabCompleteHelper sort(Comparator<String> comparator) {
        stream = stream.sorted(comparator);
        return this;
    }

    public TabCompleteHelper sortAlphabetically() {
        return sort(String.CASE_INSENSITIVE_ORDER);
    }

    public TabCompleteHelper filterPrefix(String prefix) {
        return filter(x -> x.toLowerCase(Locale.US).startsWith(prefix.toLowerCase(Locale.US)));
    }

    public TabCompleteHelper filterPrefixNamespaced(String prefix) {
        return filterPrefix(Identifier.of(prefix).toString());
    }

    public String[] build() {
        return stream.toArray(String[]::new);
    }

    public Stream<String> stream() {
        return stream;
    }

    public TabCompleteHelper addCommands(ICommandManager manager) {
        return append(manager.getRegistry().descendingStream()
                .flatMap(command -> command.getNames().stream())
                .distinct()
        );
    }



}
