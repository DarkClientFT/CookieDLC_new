package ru.cookiedlc.module.impl.movement.air;

import net.minecraft.network.packet.Packet;
import ru.cookiedlc.event.api.events.Event;


public class PacketEvent implements Event {

    private final Packet<?> packet;
    private boolean cancelled;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
        this.cancelled = false;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet);
        }
    }
}