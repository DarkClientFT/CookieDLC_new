package ru.cookiedlc.event.events.player;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.PlayerInput;
import ru.cookiedlc.event.api.events.callables.EventCancellable;

@Getter
@Setter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InputEvent extends EventCancellable {
    PlayerInput input;

    public void setJumping(boolean jump) {
        input = new PlayerInput(input.forward(), input.backward(), input.left(), input.right(), jump, input.sneak(), input.sprint());
    }

    public void setDirectional(boolean forward, boolean backward, boolean left, boolean right) {
        input = new PlayerInput(forward, backward, left, right, input.jump(), input.sneak(), input.sprint());
    }

    public void setSneak(boolean sneak) {
        input = new PlayerInput(input.forward(), input.backward(), input.left(), input.right(), input.jump(), sneak, input.sprint());
    }

    public void inputNone() {
        input = new PlayerInput(false, false, false, false, false, false, false);
    }

    public int forward() {
        return input.forward() ? 1 : input.backward() ? -1 : 0;
    }

    public float sideways() {
        return input.left() ? 1 : input.right() ? -1 : 0;
    }

    public float getForward() {
        return input.forward() ? 1.0F : input.backward() ? -1.0F : 0.0F;
    }

    public float getStrafe() {
        return input.left() ? 1.0F : input.right() ? -1.0F : 0.0F;
    }

    public boolean isJump() {
        return input.jump();
    }

    public boolean isSneak() {
        return input.sneak();
    }

    public void setForward(float forward) {
        boolean forwardKey = forward > 0;
        boolean backwardKey = forward < 0;
        input = new PlayerInput(forwardKey, backwardKey, input.left(), input.right(), input.jump(), input.sneak(), input.sprint());
    }

    public void setStrafe(float strafe) {
        boolean leftKey = strafe > 0;
        boolean rightKey = strafe < 0;
        input = new PlayerInput(input.forward(), input.backward(), leftKey, rightKey, input.jump(), input.sneak(), input.sprint());
    }

    public void setJump(boolean jump) {
        setJumping(jump);
    }
}