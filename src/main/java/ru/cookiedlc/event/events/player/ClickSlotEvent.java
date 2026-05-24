package ru.cookiedlc.event.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.screen.slot.SlotActionType;
import ru.cookiedlc.event.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class ClickSlotEvent implements Event {
    private final int syncId;
    private final int slotId;
    private final int button;
    private final SlotActionType actionType;
    private boolean cancelled;

    public ClickSlotEvent(int syncId, int slotId, int button, SlotActionType actionType) {
        this.syncId = syncId;
        this.slotId = slotId;
        this.button = button;
        this.actionType = actionType;
        this.cancelled = false;
    }
}