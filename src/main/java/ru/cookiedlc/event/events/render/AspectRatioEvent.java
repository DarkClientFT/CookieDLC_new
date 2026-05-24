package ru.cookiedlc.event.events.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.event.api.events.callables.EventCancellable;
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class AspectRatioEvent extends EventCancellable {
    float ratio;
}
