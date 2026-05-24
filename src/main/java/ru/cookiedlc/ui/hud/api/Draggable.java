package ru.cookiedlc.ui.hud.api;

import net.minecraft.client.gui.DrawContext;
import ru.cookiedlc.event.events.container.SetScreenEvent;
import ru.cookiedlc.event.events.packet.PacketEvent;

public interface Draggable {
    boolean visible();

    void tick();

    void render(DrawContext context, int mouseX, int mouseY, float delta);

    void packet(PacketEvent e);

    void setScreen(SetScreenEvent screen);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseReleased(double mouseX, double mouseY, int button);
}
