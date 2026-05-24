package ru.cookiedlc.module.impl.combat.killaura.rotation.modes.smooth;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;
import ru.cookiedlc.module.impl.combat.killaura.rotation.AngleUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RaytracingUtil;
import ru.cookiedlc.module.impl.combat.KillAura;
import ru.cookiedlc.module.impl.combat.killaura.rotation.angle.AngleSmoothMode;

public class MatrixMode extends AngleSmoothMode {
    public MatrixMode() {
        super("Matrix");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();

        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        boolean shouldAttack = entity !=null && Main.getInstance().getAttackPerpetrator().getAttackHandler().canAttack(KillAura.getInstance().getConfig(), 2);
        boolean shouldAttack2 = entity !=null && Main.getInstance().getAttackPerpetrator().getAttackHandler().canAttack(KillAura.getInstance().getConfig(), 0);
        boolean shouldAttack3 = entity !=null && Main.getInstance().getAttackPerpetrator().getAttackHandler().canAttack(KillAura.getInstance().getConfig(), 5);

        boolean rayTrace = entity !=null && RaytracingUtil.rayTrace(KillAura.getInstance().getAttackRange().getValue(), entity.getBoundingBox());

        float straightLineYaw = Math.abs(yawDelta / rotationDifference) * (shouldAttack2 ? 100 : MathUtil.randomLerp(25, 50));
        float straightLinePitch = Math.abs(pitchDelta / rotationDifference) * (shouldAttack2 ? 100 : MathUtil.randomLerp(5, 25));

        if (!Main.getInstance().getAttackPerpetrator().getAttackHandler().getAttackTimer().finished(MathUtil.randomLerp(0, 300))) {
            straightLinePitch = -4;
            straightLineYaw = -4;
        }

        float jittetY = shouldAttack3 ? !rayTrace && shouldAttack ? 0 : MathUtil.randomLerp(3, -3) : 0;
        float jittetX = shouldAttack3 ? !rayTrace && shouldAttack ? 0 : MathUtil.randomLerp(3, -3) : 0;

        return new Angle(currentAngle.getYaw() + Math.min(Math.max(yawDelta, -straightLineYaw), straightLineYaw) + jittetX, currentAngle.getPitch() + Math.min(Math.max(pitchDelta, -straightLinePitch), straightLinePitch) + jittetY);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0, 0, 0);
    }
}
