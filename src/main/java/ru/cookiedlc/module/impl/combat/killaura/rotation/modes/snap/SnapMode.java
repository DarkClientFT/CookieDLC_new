package ru.cookiedlc.module.impl.combat.killaura.rotation.modes.snap;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.module.impl.combat.KillAura;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;
import ru.cookiedlc.module.impl.combat.killaura.rotation.AngleUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.angle.AngleSmoothMode;

public class SnapMode extends AngleSmoothMode {
    public SnapMode() {
        super("Snap");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        if (entity != null && Main.getInstance().getAttackPerpetrator().getAttackHandler().canAttack(KillAura.getInstance().getConfig(), 1)) {
            Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
            float yawDelta = angleDelta.getYaw();
            float pitchDelta = angleDelta.getPitch();
            float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

            if (rotationDifference == 0) return currentAngle;

            float lineYaw = Math.abs(yawDelta / rotationDifference) * 180;
            float linePitch = Math.abs(pitchDelta / rotationDifference) * 180;

            float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

            float speed = 1.0F;
            float lerpYaw = MathHelper.lerp(MathUtil.getRandom(speed, speed + 0.2F), currentAngle.getYaw(),
                    currentAngle.getYaw() + moveYaw);
            float lerpPitch = MathHelper.lerp(MathUtil.getRandom(speed, speed + 0.2F), currentAngle.getPitch(),
                    currentAngle.getPitch() + movePitch);

            return new Angle(lerpYaw, lerpPitch);
        } else {
            Angle playerViewAngle = new Angle(mc.player.getYaw(), mc.player.getPitch());
            Angle angleDelta = AngleUtil.calculateDelta(currentAngle, playerViewAngle);
            float yawDelta = angleDelta.getYaw();
            float pitchDelta = angleDelta.getPitch();
            float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

            if (rotationDifference < 0.1F) {
                return playerViewAngle;
            }

            float returnSpeed = !Main.getInstance().getAttackPerpetrator().getAttackHandler().getAttackTimer().finished(380) ? 0.0F : 0.4F;

            if (returnSpeed <= 0.001F) {
                return currentAngle;
            }

            float maxDeltaPerTick = 50.0F;

            float moveYaw = MathHelper.clamp(yawDelta * returnSpeed, -maxDeltaPerTick, maxDeltaPerTick);
            float movePitch = MathHelper.clamp(pitchDelta * returnSpeed, -maxDeltaPerTick, maxDeltaPerTick);

            float newYaw = currentAngle.getYaw() + moveYaw;
            float newPitch = MathHelper.clamp(currentAngle.getPitch() + movePitch, -90, 90);

            return new Angle(newYaw, newPitch);
        }
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.12F, 0.12F, 0.12F);
    }
}
