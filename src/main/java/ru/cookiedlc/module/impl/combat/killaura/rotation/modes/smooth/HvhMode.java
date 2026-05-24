package ru.cookiedlc.module.impl.combat.killaura.rotation.modes.smooth;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;
import ru.cookiedlc.module.impl.combat.killaura.rotation.angle.AngleSmoothMode;

public class HvhMode extends AngleSmoothMode {
    public HvhMode() {
        super("HvH");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        float speed = 360000000000.0F;
        float yawDiff = MathHelper.wrapDegrees(targetAngle.getYaw() - currentAngle.getYaw());
        float yawChange = MathHelper.clamp(yawDiff, -speed, speed);

        return new Angle(
                currentAngle.getYaw() + yawChange,
                currentAngle.getPitch()
        );
    }

    @Override
    public Vec3d randomValue() {
        return Vec3d.ZERO;
    }
}