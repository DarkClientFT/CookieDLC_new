package ru.cookiedlc.module.impl.combat.killaura.rotation;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.common.QuickImports;

import static ru.cookiedlc.module.impl.combat.killaura.rotation.FromVector.getBestPoint;


public class FinalVector implements QuickImports {

    public static Vec3d expensiveUpgradePoint(Entity entity) {
        Vec3d aimPoint = FromVector.hitbox(entity, 1, 1, 1, 4);
        return aimPoint;
    }

    public static Vec3d mincedPoint(Entity entity) {
        Vec3d aimPoint = getBestPoint(mc.player.getEyePos(), entity);
        return aimPoint;
    }

    public static Vec3d celestialPoint(Entity entity) {
        Vec3d aimPoint = FromVector.closest(entity);
        return aimPoint;
    }

    public static Vec3d randomPoint(Entity entity) {
        Vec3d aimPoint = FromVector.custom(entity, 75, 1000);
        return aimPoint;
    }


}