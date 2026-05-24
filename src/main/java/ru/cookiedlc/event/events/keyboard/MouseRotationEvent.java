package ru.cookiedlc.event.events.keyboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.cookiedlc.event.api.events.callables.EventCancellable;

@Getter
@Setter
@AllArgsConstructor
public class MouseRotationEvent extends EventCancellable {
    float cursorDeltaX, cursorDeltaY;
}
