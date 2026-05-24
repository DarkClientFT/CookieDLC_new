package ru.cookiedlc.module.impl.player.scafold;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.event.api.events.Event;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SafeWalkEvent implements Event {

    boolean safe;

    public SafeWalkEvent() {
        this.safe = false;
    }
}