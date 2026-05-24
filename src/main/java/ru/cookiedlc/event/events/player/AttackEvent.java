package ru.cookiedlc.event.events.player;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import ru.cookiedlc.event.api.events.callables.EventCancellable;

@AllArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttackEvent extends EventCancellable {
    Entity entity;
}
