package ru.cookiedlc.event.api.events;

public interface Cancellable {

    boolean isCancelled();

    void cancel();

}