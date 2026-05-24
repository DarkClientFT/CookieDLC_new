package ru.cookiedlc.event.events.block;

import net.minecraft.util.math.BlockPos;
import ru.cookiedlc.event.api.events.Event;

public record BreakBlockEvent(BlockPos blockPos) implements Event {}
