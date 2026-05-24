package ru.cookiedlc.event.events.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.cookiedlc.event.api.events.callables.EventCancellable;

@Getter
@Setter
@AllArgsConstructor
public class UsingItemEvent extends EventCancellable {
    byte type;
}
