package ru.cookiedlc.event.api.events.callables;

import lombok.Setter;
import ru.cookiedlc.event.api.events.Cancellable;
import ru.cookiedlc.event.api.events.Event;

public abstract class EventCancellable implements Event, Cancellable {

    @Setter
    private boolean cancelled;

    protected EventCancellable() {
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }


    @Override
    public void cancel() {
        cancelled = true;
    }
}