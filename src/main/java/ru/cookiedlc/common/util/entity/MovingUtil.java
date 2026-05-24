package ru.cookiedlc.common.util.entity;

import lombok.experimental.UtilityClass;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationController;

import java.util.Objects;
import java.util.stream.Stream;

@UtilityClass
public class MovingUtil implements QuickImports {

    public boolean hasPlayerMovement() {
        return mc.player.input.movementForward != 0f || mc.player.input.movementSideways != 0f;
    }
    public static KeyBinding[] getMovementKeys(boolean includeSneak) {
        return Stream.of(
                        mc.options.forwardKey,
                        mc.options.backKey,
                        mc.options.leftKey,
                        mc.options.rightKey,
                        mc.options.jumpKey,
                        mc.options.sprintKey,
                        includeSneak ? mc.options.sneakKey : null
                ).filter(Objects::nonNull)
                .toArray(KeyBinding[]::new);
    }
    public double[] calculateDirection(double distance) {
        return calculateDirection(mc.player.input.movementForward, mc.player.input.movementSideways, distance);
    }

    public static double[] forward(final double d) {
        float f = mc.player.input.movementForward;
        float f2 = mc.player.input.movementSideways;
        float f3 = RotationController.INSTANCE.getRotation().getYaw();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += ((f > 0.0f) ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += ((f > 0.0f) ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        final double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        final double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        final double d4 = f * d * d3 + f2 * d * d2;
        final double d5 = f * d * d2 - f2 * d * d3;
        return new double[]{d4, d5};
    }
    public double[] calculateDirection(float forward, float sideways, double distance) {
        float yaw = mc.player.getYaw();
        if (forward != 0.0f) {
            if (sideways > 0.0f) {
                yaw += (forward > 0.0f) ? -45 : 45;
            } else if (sideways < 0.0f) {
                yaw += (forward > 0.0f) ? 45 : -45;
            }
            sideways = 0.0f;
            forward = (forward > 0.0f) ? 1.0f : -1.0f;
        }

        double sinYaw = Math.sin(Math.toRadians(yaw + 90.0f));
        double cosYaw = Math.cos(Math.toRadians(yaw + 90.0f));
        double xMovement = forward * distance * cosYaw + sideways * distance * sinYaw;
        double zMovement = forward * distance * sinYaw - sideways * distance * cosYaw;

        return new double[]{xMovement, zMovement};
    }

    public double getSpeedSqrt(Entity entity) {
        return Math.sqrt(entity.squaredDistanceTo(new Vec3d(entity.prevX, entity.prevY, entity.prevZ)));
    }

    public void setVelocity(double velocity) {
        final double[] direction = MovingUtil.calculateDirection(velocity);
        Objects.requireNonNull(mc.player).setVelocity(direction[0], mc.player.getVelocity().getY(), direction[1]);
    }

    public void setVelocity(double velocity, double y) {
        final double[] direction = MovingUtil.calculateDirection(velocity);
        Objects.requireNonNull(mc.player).setVelocity(direction[0], y, direction[1]);
    }

    public double getDegreesRelativeToView(
            Vec3d positionRelativeToPlayer,
            float yaw) {

        float optimalYaw =
                (float) Math.atan2(-positionRelativeToPlayer.x, positionRelativeToPlayer.z);
        double currentYaw = Math.toRadians(MathHelper.wrapDegrees(yaw));

        return Math.toDegrees(MathHelper.wrapDegrees((optimalYaw - currentYaw)));
    }

    public PlayerInput getDirectionalInputForDegrees(PlayerInput input, double dgs, float deadAngle) {
        boolean forwards = input.forward();
        boolean backwards = input.backward();
        boolean left = input.left();
        boolean right = input.right();

        if (dgs >= (-90.0F + deadAngle) && dgs <= (90.0F - deadAngle)) {
            forwards = true;
        } else if (dgs < (-90.0F - deadAngle) || dgs > (90.0F + deadAngle)) {
            backwards = true;
        }

        if (dgs >= (0.0F + deadAngle) && dgs <= (180.0F - deadAngle)) {
            right = true;
        } else if (dgs >= (-180.0F + deadAngle) && dgs <= (0.0F - deadAngle)) {
            left = true;
        }

        return new PlayerInput(forwards, backwards, left, right, input.jump(), input.sneak(), input.sprint());
    }

    public PlayerInput getDirectionalInputForDegrees(PlayerInput input, double dgs) {
        return getDirectionalInputForDegrees(input, dgs, 20.0F);
    }
}
