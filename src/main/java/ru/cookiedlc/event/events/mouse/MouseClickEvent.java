package ru.cookiedlc.event.events.mouse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.cookiedlc.event.api.events.Event;

@Getter
@AllArgsConstructor
public class MouseClickEvent implements Event {
    private final int button;
    private final int action;
    private final int mods;

    public boolean isLeftClick() {
        return button == 0 && action == 1;
    }

    public boolean isRightClick() {
        return button == 1 && action == 1;
    }

    public boolean isMiddleClick() {
        return button == 2 && action == 1;
    }
}