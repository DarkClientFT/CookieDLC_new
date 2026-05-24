package ru.cookiedlc.module.impl.combat.killaura.rotation.modes.smooth;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.module.impl.combat.KillAura;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;
import ru.cookiedlc.module.impl.combat.killaura.rotation.AngleUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RaytracingUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.angle.AngleSmoothMode;

import java.security.SecureRandom;

public class MindAIMode extends AngleSmoothMode {

    public MindAIMode() {
        super("SlothAC");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw(), pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        KillAura aura = KillAura.getInstance();
        boolean атака = lolikbypass(0);
        boolean pa = aura.getTarget() != null && RaytracingUtil.rayTrace(currentAngle.getYaw(), currentAngle.getPitch(), aura.getAttackRange().getValue(), aura.getRotationRange().getValue(), aura.getTarget());
        float pitch = 0;
        if (aura.getTarget() != null && !атака) {
            pitch = (float) (9999 * Math.cos(System.currentTimeMillis() / 0.1D));
        }

        float yaw = 0;
        if (aura.getTarget() != null && !атака) {
            yaw = (float) (9999 * Math.sin(System.currentTimeMillis() / 0.1D));
        }
        float скорост = атака ? 115555555555555F : (lolikbypass(0) ? 1 : 1.1F);
        if (атака && !pa) {
            скорост = 115555555555555F;
        }
        float lineYaw = (Math.abs(yawDelta  / rotationDifference) * 180);
        float linePitch = (Math.abs(pitchDelta ) * 180);
        float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = MathHelper.clamp(pitchDelta , -linePitch, linePitch);
        float targetYaw = currentAngle.getYaw() + moveYaw;

        Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(MathHelper.lerp(Math.clamp(randomLerp(скорост, скорост + 0.2F), 0f, 1f), currentAngle.getYaw(), targetYaw) + yaw);
        moveAngle.setPitch(MathHelper.lerp(Math.clamp(randomLerp(скорост, скорост + 0.2F), 0f, 1f), currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + pitch);
        return moveAngle;
    }

    private boolean lolikbypass(int ticks) {
        KillAura aura = KillAura.getInstance();
        return aura.getTarget() != null && Main.getInstance().getAttackPerpetrator().getAttackHandler().canAttack(aura.getConfig(), ticks);
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(new SecureRandom().nextFloat(), min, max);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0, 0, 0);
    }
}
