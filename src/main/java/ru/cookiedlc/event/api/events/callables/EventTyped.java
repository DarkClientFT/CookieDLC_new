package ru.cookiedlc.event.api.events.callables;

import ru.cookiedlc.event.api.events.Event;
import ru.cookiedlc.event.api.events.Typed;

public abstract class EventTyped implements Event, Typed {

    private final byte type;

    protected EventTyped(byte eventType) {
        type = eventType;
    }

    @Override
    public byte getType() {
        return type;
    }

}