package ru.cookiedlc.module.impl.combat.killaura.rotation;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.common.QuickImports;

import static java.lang.Math.toDegrees;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@UtilityClass
public class AngleUtil implements QuickImports {
    public Angle fromVec2f(Vec2f vector2f) {
        return new Angle(vector2f.y, vector2f.x);
    }

    public Angle fromVec3d(Vec3d vector) {
        if (mc.player == null) return new Angle(0.0F, 0.0F);

        double x = vector.x;
        double y = vector.y;
        double z = vector.z;

        float yaw = (float) wrapDegrees(toDegrees(Math.atan2(z, x)) - 90.0);
        float pitch = (float) toDegrees(-Math.atan2(y, Math.sqrt(x * x + z * z)));
        pitch = MathHelper.clamp(pitch, -90.0F, 90.0F);

        return new Angle(yaw, pitch);
    }

    public Angle calculateDelta(Angle start, Angle end) {
        float deltaYaw = MathHelper.wrapDegrees(end.getYaw() - start.getYaw());
        float deltaPitch = MathHelper.wrapDegrees(end.getPitch() - start.getPitch());
        return new Angle(deltaYaw, deltaPitch);
    }

    public Angle calculateAngle(Vec3d to) {
        return fromVec3d(to.subtract(mc.player.getEyePos()));
    }

    public Angle pitch(float pitch) {
        return new Angle(mc.player.getYaw(), pitch);
    }

    public Angle cameraAngle() {return new Angle(mc.player.getYaw(), mc.player.getPitch());}

}
