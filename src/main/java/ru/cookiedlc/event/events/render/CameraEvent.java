package ru.cookiedlc.event.events.render;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.event.api.events.callables.EventCancellable;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CameraEvent extends EventCancellable {
    boolean cameraClip;
    float distance;
    Angle angle;
}
