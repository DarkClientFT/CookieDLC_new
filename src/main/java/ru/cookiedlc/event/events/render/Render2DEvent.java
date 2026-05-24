package ru.cookiedlc.event.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import ru.cookiedlc.event.api.events.Event;

@Getter
@AllArgsConstructor
public class Render2DEvent implements Event {
    private final DrawContext context;
    private final float tickDelta;
}