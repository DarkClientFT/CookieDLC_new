package ru.cookiedlc.event.events.chat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.event.api.events.Event;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TabToggleEvent implements Event {
    boolean open;

    public TabToggleEvent(boolean open) {
        this.open = open;
    }
}
