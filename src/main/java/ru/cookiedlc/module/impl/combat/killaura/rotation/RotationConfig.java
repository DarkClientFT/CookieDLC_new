package ru.cookiedlc.module.impl.combat.killaura.rotation;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.module.impl.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.cookiedlc.module.impl.combat.killaura.rotation.modes.smooth.LinearMode;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RotationConfig {
    public static RotationConfig DEFAULT = new RotationConfig(new LinearMode(), true, true, false, null);
    boolean moveCorrection, freeCorrection, targetedCorrection;
    Entity targetEntity;
    AngleSmoothMode angleSmooth;
    int resetThreshold = 3;

    public RotationConfig(boolean moveCorrection, boolean freeCorrection) {
        this(new LinearMode(), moveCorrection, freeCorrection, false, null);
    }

    public RotationConfig(boolean moveCorrection) {
        this(new LinearMode(), moveCorrection, true, false, null);
    }

    public RotationConfig(AngleSmoothMode angleSmooth, boolean moveCorrection, boolean freeCorrection) {
        this(angleSmooth, moveCorrection, freeCorrection, false, null);
    }

    public RotationConfig(AngleSmoothMode angleSmooth, boolean moveCorrection, boolean freeCorrection, boolean targetedCorrection, Entity targetEntity) {
        this.angleSmooth = angleSmooth;
        this.moveCorrection = moveCorrection;
        this.freeCorrection = freeCorrection;
        this.targetedCorrection = targetedCorrection;
        this.targetEntity = targetEntity;
    }

    public RotationPlan createRotationPlan(Angle angle, Vec3d vec, Entity entity, int reset) {
        return new RotationPlan(angle, vec, entity, angleSmooth, reset, resetThreshold, moveCorrection, freeCorrection, targetedCorrection, targetEntity);
    }

    public RotationPlan createRotationPlan(Angle angle, Vec3d vec, Entity entity, boolean moveCorrection, boolean freeCorrection) {
        return new RotationPlan(angle, vec, entity, angleSmooth, 1, resetThreshold, moveCorrection, freeCorrection, targetedCorrection, targetEntity);
    }
}