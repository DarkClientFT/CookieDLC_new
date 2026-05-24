package ru.cookiedlc.module.impl.movement.noclip;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import ru.cookiedlc.event.api.events.Event;

@Getter
@Setter
public class BlockCollisionEvent implements Event {
    private BlockPos blockPos;
    private BlockState state;

    public BlockCollisionEvent(BlockPos blockPos, BlockState state) {
        this.blockPos = blockPos;
        this.state = state;
    }
}

