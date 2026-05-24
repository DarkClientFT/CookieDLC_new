package ru.cookiedlc.module.impl.combat.killaura.rotation.modes.snap;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.module.impl.combat.KillAura;
import ru.cookiedlc.module.impl.combat.killaura.attack.AttackHandler;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;
import ru.cookiedlc.module.impl.combat.killaura.rotation.AngleUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.angle.AngleSmoothMode;

import java.security.SecureRandom;

public class FTTestMode extends AngleSmoothMode {
    private final SecureRandom random = new SecureRandom();

    public FTTestMode() {
        super("FTEST");}

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        AttackHandler attackHandler = Main.getInstance().getAttackPerpetrator().getAttackHandler();
        StopWatch attackTimer = attackHandler.getAttackTimer();
        int count = attackHandler.getCount();
        Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw(), pitchDelta = angleDelta.getPitch();
        float rotationDifference = Math.max((float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta)), 1.0E-4F);
        if (entity != null) {
            float speed = attackHandler.canAttack(KillAura.getInstance().getConfig(), 0)
                    ? random.nextBoolean() ? 0.97F : 0.85F : random.nextBoolean() ? 0.4F : 0.2F;
            float lineYaw = (Math.abs(yawDelta / rotationDifference) * 105);
            float linePitch = (Math.abs(pitchDelta / rotationDifference) * 105);
            float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);
            float jitterScale = getJitterScale(entity, vec3d, attackTimer);
            float jitterYaw = randomLerp(-0.55F, 0.55F) * jitterScale + getSineJitter(0.42F * jitterScale, 175.0);
            float jitterPitch = randomLerp(-0.32F, 0.32F) * jitterScale + getSineJitter(0.24F * jitterScale, 230.0);
            float retreatYaw = getPostAttackRetreatYaw(attackTimer, count) * jitterScale;
            float retreatPitch = getPostAttackRetreatPitch(attackTimer) * jitterScale;
            Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
            moveAngle.setYaw(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + jitterYaw + retreatYaw);
            moveAngle.setPitch(MathHelper.lerp(randomLerp(speed, speed + 0.2F), currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + jitterPitch + retreatPitch);
            return moveAngle;} else {
            int suck = count % 3;
            float speed = attackTimer.finished(400) ? random.nextBoolean() ? 0.4F : 0.2F : -0.2F;
            float random = attackTimer.elapsedTime() / 40F + (count % 6);
            Angle randomAngle = switch (suck) {
                case 0 -> new Angle((float) Math.cos(random), (float) Math.sin(random));
                case 1 -> new Angle((float) Math.sin(random), (float) Math.cos(random));
                case 2 -> new Angle((float) Math.sin(random), (float) -Math.cos(random));
                default -> new Angle((float) -Math.cos(random), (float) Math.sin(random));};
            float yaw = !attackTimer.finished(2000) ? randomLerp(12, 24) * randomAngle.getYaw() : 0;
            float pitch2 = randomLerp(0, 2) * (float) Math.cos((double) System.currentTimeMillis() / 5000);
            float pitch = !attackTimer.finished(2000) ? randomLerp(2, 6) * randomAngle.getPitch() + pitch2 : 0;
            float lineYaw = (Math.abs(yawDelta / rotationDifference) * 35);
            float linePitch = (Math.abs(pitchDelta / rotationDifference) * 35);
            float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
            float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);
            Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
            moveAngle.setYaw(MathHelper.lerp(Math.clamp(randomLerp(speed, speed + 0.2F), 0, 1), currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + yaw);
            moveAngle.setPitch(MathHelper.lerp(Math.clamp(randomLerp(speed, speed + 0.2F), 0, 1), currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + pitch);
            return moveAngle;}}
    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.035, 0.06, 0.035);}
    private float getJitterScale(Entity entity, Vec3d vec3d, StopWatch attackTimer) {
        Vec3d point = vec3d != null ? vec3d : entity.getBoundingBox().getCenter();
        double distance = mc.player.getEyePos().distanceTo(point);
        float scale = (float) MathHelper.clamp(1.15F - distance * 0.12F, 0.45F, 1.0F);
        return attackTimer.elapsedTime() < 160 ? scale * 0.82F : scale;}
    private float getSineJitter(float amplitude, double speedDivisor) {
        return amplitude * (float) Math.sin(System.currentTimeMillis() / speedDivisor);}
    private float getPostAttackRetreatYaw(StopWatch attackTimer, int count) {
        long elapsed = attackTimer.elapsedTime();
        if (elapsed < 35L || elapsed > 140L) return 0F;
        float fade = 1F - (elapsed - 35F) / 105F;
        float side = count % 2 == 0 ? 1F : -1F;
        return side * randomLerp(1.1F, 2.0F) * fade;}
    private float getPostAttackRetreatPitch(StopWatch attackTimer) {
        long elapsed = attackTimer.elapsedTime();
        if (elapsed < 35L || elapsed > 140L) return 0F;
        float fade = 1F - (elapsed - 35F) / 105F;
        return randomLerp(0.2F, 0.75F) * fade;}
    private float randomLerp(float min, float max) {
        return MathHelper.lerp(random.nextFloat(), min, max);}
}