package ru.cookiedlc.module.impl.combat.killaura.rotation.modes.smooth;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.module.impl.combat.KillAura;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;
import ru.cookiedlc.module.impl.combat.killaura.rotation.AngleUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.angle.AngleSmoothMode;

public class SmoothMode extends AngleSmoothMode {

    public SmoothMode() {
        super("Smooth");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        boolean canAttack = entity !=null && Main.getInstance().getAttackPerpetrator().getAttackHandler().canAttack(KillAura.getInstance().getConfig(), 0);
        Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        float straightLineYaw = Math.abs(yawDelta / rotationDifference) * 45;
        float straightLinePitch = Math.abs(pitchDelta / rotationDifference) * 25;

        return new Angle(currentAngle.getYaw() + Math.min(Math.max(yawDelta, -straightLineYaw), straightLineYaw) + MathUtil.randomLerp(-1, 1), currentAngle.getPitch() + Math.min(Math.max(pitchDelta, -straightLinePitch), straightLinePitch) + MathUtil.randomLerp(-1, 1));
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0, 0, 0);
    }
}
