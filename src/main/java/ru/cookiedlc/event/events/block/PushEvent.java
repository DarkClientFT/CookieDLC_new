package ru.cookiedlc.event.events.block;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.cookiedlc.event.api.events.callables.EventCancellable;

@Getter
@AllArgsConstructor
public class PushEvent extends EventCancellable {
    private Type type;

    public enum Type {
        COLLISION, BLOCK, WATER
    }
}
