package ru.cookiedlc.event.events.player;

import lombok.Getter;
import lombok.Setter;
import ru.cookiedlc.event.api.events.Event;

@Getter
@Setter
public class RotationUpdateEvent implements Event {
    private byte type;
    private float yaw;
    private float pitch;
    private boolean modifyRotation = false;

    public RotationUpdateEvent(byte type) {
        this.type = type;
    }

    public RotationUpdateEvent(byte type, float yaw, float pitch) {
        this.type = type;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        this.modifyRotation = true;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        this.modifyRotation = true;
    }

    public void setRotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.modifyRotation = true;
    }

    public boolean shouldModifyRotation() {
        return modifyRotation;
    }
}