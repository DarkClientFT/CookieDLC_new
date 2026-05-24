package ru.cookiedlc.event.events.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ru.cookiedlc.event.api.events.Event;

public record BlockBreakingEvent(BlockPos blockPos, Direction direction) implements Event {}
