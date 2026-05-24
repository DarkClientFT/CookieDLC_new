package ru.cookiedlc.event.events.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import ru.cookiedlc.event.api.events.Event;

public record BlockUpdateEvent(BlockState state, BlockPos pos, Type type) implements Event {
    public enum Type {
        LOAD, UNLOAD, UPDATE
    }
}
