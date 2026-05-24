package ru.cookiedlc.event.events.block;

import net.minecraft.block.entity.BlockEntity;
import ru.cookiedlc.event.api.events.Event;

public record BlockEntityProgressEvent(BlockEntity blockEntity, Type type) implements Event {
    public enum Type {
        ADD, REMOVE
    }
}
