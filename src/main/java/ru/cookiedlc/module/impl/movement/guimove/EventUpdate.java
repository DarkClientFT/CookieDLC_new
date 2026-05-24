package ru.cookiedlc.module.impl.movement.guimove;


import ru.cookiedlc.event.api.events.Event;

public class EventUpdate implements Event {
    EventUpdate() {
    }

    public static class Slow extends EventUpdate {
        public Slow() {
            super();
        }
    }

    public static class Fast extends EventUpdate {
        public Fast() {
            super();
        }
    }

    public static class Fastest extends EventUpdate {
        public Fastest() {
            super();
        }
    }
}