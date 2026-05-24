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
import ru.cookiedlc.module.impl.misc.autoduel.TimerUtil;
import java.security.SecureRandom;

public class KallMode extends AngleSmoothMode {

    private static final float BASE_SPEED = 0.85F;
    private static final float SLOW_SPEED = 0.3F;
    private static final float CORNER_TRANSITION_SPEED = 0.2F;

    private static final float[][] CORNER_OFFSETS = {
            {-20.0F, -16.0F},
            {20.0F, -16.0F},
            {-20.0F, 16.0F},
            {20.0F, 16.0F},
            {0.0F, -16.0F},
            {0.0F, 16.0F},
            {-22.0F, 0.0F},
            {22.0F, 0.0F},
            {-15.0F, -10.0F},
            {15.0F, 10.0F},
            {-25.0F, -8.0F},
            {25.0F, -8.0F},
            {-12.0F, 18.0F},
            {12.0F, -18.0F},
    };

    private static final float SHAKE_INTENSITY_MIN = 4.0F;
    private static final float SHAKE_INTENSITY_MAX = 12.0F;
    private static final float SHAKE_PITCH_MIN = 2.0F;
    private static final float SHAKE_PITCH_MAX = 8.0F;

    private static final float SPIKE_CHANCE = 0.15F;
    private static final float SPIKE_YAW_MIN = 15.0F;
    private static final float SPIKE_YAW_MAX = 35.0F;
    private static final float SPIKE_PITCH_MIN = 8.0F;
    private static final float SPIKE_PITCH_MAX = 20.0F;

    private final SecureRandom random = new SecureRandom();

    private int currentCorner = 0;
    private int ticksInCorner = 0;
    private int cornerDuration = 0;
    private float cornerYawOffset = 0.0F;
    private float cornerPitchOffset = 0.0F;
    private float targetCornerYaw = 0.0F;
    private float targetCornerPitch = 0.0F;
    private long lastCornerSwitch = 0L;

    private float wavePhase = 0.0F;
    private float waveAmplitudeYaw = 0.0F;
    private float waveAmplitudePitch = 0.0F;

    private float currentSpikeYaw = 0.0F;
    private float currentSpikePitch = 0.0F;
    private int spikeCooldown = 0;

    private float shakeYaw = 0.0F;
    private float shakePitch = 0.0F;
    private boolean isActive = false;
    public KallMode() {
        super("KallMode");
    }
    public void onEnable() {
        isActive = true;

    }


    public void onDisable() {
        isActive = false;
        reset();
    }
    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        AttackHandler attackHandler = Main.getInstance().getAttackPerpetrator().getAttackHandler();
        StopWatch attackTimer = attackHandler.getAttackTimer();
        int attackCount = attackHandler.getCount();

        Angle angleDelta = AngleUtil.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        if (rotationDifference < 0.01F) {
            rotationDifference = 0.01F;
        }

        updateShake();

        updateSpikes();

        if (entity != null) {
            return handleTargetMode(currentAngle, targetAngle, yawDelta, pitchDelta,
                    rotationDifference, attackHandler, attackTimer, attackCount);
        } else {
            return handleIdleMode(currentAngle, targetAngle, yawDelta, pitchDelta,
                    rotationDifference, attackTimer, attackCount);
        }

    }

    private Angle handleTargetMode(Angle currentAngle, Angle targetAngle,
                                   float yawDelta, float pitchDelta, float rotationDifference,
                                   AttackHandler attackHandler, StopWatch attackTimer, int attackCount) {

        long currentTime = System.currentTimeMillis();

        updateCornerTransition(currentTime, attackCount, true);
        updateWaveMotion(currentTime);

        boolean canAttack = attackHandler.canAttack(KillAura.getInstance().getConfig(), 0);
        float speed;

        if (canAttack) {
            speed = BASE_SPEED + randomLerp(0.0F, 0.15F);
        } else if (!attackTimer.finished(200)) {
            speed = SLOW_SPEED + randomLerp(0.0F, 0.2F);
        } else {
            speed = randomLerp(0.5F, 0.7F);
        }

        float lineYaw = Math.abs(yawDelta / rotationDifference) * 180.0F;
        float linePitch = Math.abs(pitchDelta / rotationDifference) * 180.0F;

        float totalYawOffset = cornerYawOffset + shakeYaw + currentSpikeYaw;
        float totalPitchOffset = cornerPitchOffset + shakePitch + currentSpikePitch;

        totalYawOffset += waveAmplitudeYaw * (float) Math.sin(wavePhase);
        totalPitchOffset += waveAmplitudePitch * (float) Math.cos(wavePhase * 0.7F);

        float adjustedYawDelta = yawDelta + totalYawOffset;
        float adjustedPitchDelta = pitchDelta + totalPitchOffset;

        float moveYaw = MathHelper.clamp(adjustedYawDelta, -lineYaw - 30.0F, lineYaw + 30.0F);
        float movePitch = MathHelper.clamp(adjustedPitchDelta, -linePitch - 20.0F, linePitch + 20.0F);

        float newYaw = MathHelper.lerp(randomLerp(speed, speed + 0.1F),
                currentAngle.getYaw(),
                currentAngle.getYaw() + moveYaw);
        float newPitch = MathHelper.lerp(randomLerp(speed, speed + 0.1F),
                currentAngle.getPitch(),
                currentAngle.getPitch() + movePitch);

        if (random.nextFloat() < 0.4F) {
            newYaw += randomLerp(-2.5F, 2.5F);
            newPitch += randomLerp(-1.5F, 1.5F);
        }

        return new Angle(newYaw, MathHelper.clamp(newPitch, -90.0F, 90.0F));
    }

    private Angle handleIdleMode(Angle currentAngle, Angle targetAngle,
                                 float yawDelta, float pitchDelta, float rotationDifference,
                                 StopWatch attackTimer, int attackCount) {

        long currentTime = System.currentTimeMillis();

        updateCornerTransition(currentTime, attackCount, false);

        int movementPattern = attackCount % 5;
        float timeRandom = attackTimer.elapsedTime() / 40.0F + (attackCount % 10);

        float patternYaw, patternPitch;
        switch (movementPattern) {
            case 0 -> {

                patternYaw = (float) Math.cos(timeRandom) * randomLerp(15.0F, 25.0F);
                patternPitch = (float) Math.sin(timeRandom) * randomLerp(8.0F, 15.0F);
            }
            case 1 -> {

                patternYaw = (float) Math.sin(timeRandom * 1.5F) * randomLerp(18.0F, 30.0F);
                patternPitch = (float) Math.cos(timeRandom * 0.8F) * randomLerp(6.0F, 12.0F);
            }
            case 2 -> {

                patternYaw = (float) (Math.sin(timeRandom * 2.0F) + Math.cos(timeRandom * 3.1F)) * randomLerp(12.0F, 22.0F);
                patternPitch = (float) (Math.sin(timeRandom * 1.5F) - Math.cos(timeRandom * 0.7F)) * randomLerp(5.0F, 10.0F);
            }
            case 3 -> {

                float pulse = (float) Math.sin(timeRandom * 0.5F);
                patternYaw = pulse * randomLerp(20.0F, 35.0F) * (float) Math.cos(timeRandom * 2.0F);
                patternPitch = pulse * randomLerp(8.0F, 16.0F) * (float) Math.sin(timeRandom * 1.5F);
            }
            default -> {

                patternYaw = (float) Math.signum(Math.sin(timeRandom * 1.2F)) * randomLerp(10.0F, 20.0F)
                        + (float) Math.sin(timeRandom * 3.0F) * 5.0F;
                patternPitch = (float) Math.cos(timeRandom * 0.6F) * randomLerp(4.0F, 10.0F);
            }
        }

        patternYaw += cornerYawOffset * 0.7F + shakeYaw * 0.5F + currentSpikeYaw * 0.3F;
        patternPitch += cornerPitchOffset * 0.7F + shakePitch * 0.5F + currentSpikePitch * 0.3F;

        float speed;
        if (!attackTimer.finished(500)) {
            speed = randomLerp(0.1F, 0.25F);
        } else if (!attackTimer.finished(2000)) {
            speed = randomLerp(0.2F, 0.4F);
        } else {
            speed = randomLerp(0.35F, 0.55F);
            patternYaw *= 0.4F;
            patternPitch *= 0.4F;
        }

        float lineYaw = Math.abs(yawDelta / rotationDifference) * 180.0F;
        float linePitch = Math.abs(pitchDelta / rotationDifference) * 180.0F;

        float moveYaw = MathHelper.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = MathHelper.clamp(pitchDelta, -linePitch, linePitch);

        float newYaw = MathHelper.lerp(MathHelper.clamp(speed, 0.0F, 1.0F),
                currentAngle.getYaw(),
                currentAngle.getYaw() + moveYaw) + patternYaw;
        float newPitch = MathHelper.lerp(MathHelper.clamp(speed, 0.0F, 1.0F),
                currentAngle.getPitch(),
                currentAngle.getPitch() + movePitch) + patternPitch;

        return new Angle(newYaw, MathHelper.clamp(newPitch, -90.0F, 90.0F));
    }


    private void updateShake() {

        float intensity = randomLerp(SHAKE_INTENSITY_MIN, SHAKE_INTENSITY_MAX);
        float pitchIntensity = randomLerp(SHAKE_PITCH_MIN, SHAKE_PITCH_MAX);

        float targetShakeYaw = (random.nextFloat() - 0.5F) * 2.0F * intensity;
        float targetShakePitch = (random.nextFloat() - 0.5F) * 2.0F * pitchIntensity;

        shakeYaw = MathHelper.lerp(0.3F, shakeYaw, targetShakeYaw);
        shakePitch = MathHelper.lerp(0.3F, shakePitch, targetShakePitch);
    }


    private void updateSpikes() {
        if (spikeCooldown > 0) {
            spikeCooldown--;

            currentSpikeYaw *= 0.7F;
            currentSpikePitch *= 0.7F;
        } else {

            if (random.nextFloat() < SPIKE_CHANCE) {
                currentSpikeYaw = randomLerp(-SPIKE_YAW_MAX, SPIKE_YAW_MAX);
                if (Math.abs(currentSpikeYaw) < SPIKE_YAW_MIN) {
                    currentSpikeYaw = Math.signum(currentSpikeYaw) * SPIKE_YAW_MIN;
                }

                currentSpikePitch = randomLerp(-SPIKE_PITCH_MAX, SPIKE_PITCH_MAX);
                if (Math.abs(currentSpikePitch) < SPIKE_PITCH_MIN) {
                    currentSpikePitch = Math.signum(currentSpikePitch) * SPIKE_PITCH_MIN;
                }

                spikeCooldown = 8 + random.nextInt(15);
            } else {
                currentSpikeYaw *= 0.85F;
                currentSpikePitch *= 0.85F;
            }
        }

        if (Math.abs(currentSpikeYaw) < 0.5F) currentSpikeYaw = 0.0F;
        if (Math.abs(currentSpikePitch) < 0.5F) currentSpikePitch = 0.0F;
    }



    private void updateCornerTransition(long currentTime, int attackCount, boolean hasTarget) {
        ticksInCorner++;

        if (cornerDuration <= 0) {
            cornerDuration = 10 + random.nextInt(20);
        }

        boolean shouldSwitch = ticksInCorner >= cornerDuration;

        if (hasTarget && (currentTime - lastCornerSwitch) > 300 + random.nextInt(200)) {
            shouldSwitch = true;
        }

        if (shouldSwitch) {
            int newCorner;
            do {
                newCorner = random.nextInt(CORNER_OFFSETS.length);
            } while (newCorner == currentCorner && CORNER_OFFSETS.length > 1);

            currentCorner = newCorner;
            ticksInCorner = 0;
            cornerDuration = 10 + random.nextInt(20);
            lastCornerSwitch = currentTime;

            float[] offset = CORNER_OFFSETS[currentCorner];
            targetCornerYaw = offset[0] * randomLerp(0.6F, 1.5F);
            targetCornerPitch = offset[1] * randomLerp(0.6F, 1.5F);
        }

        cornerYawOffset = MathHelper.lerp(CORNER_TRANSITION_SPEED, cornerYawOffset, targetCornerYaw);
        cornerPitchOffset = MathHelper.lerp(CORNER_TRANSITION_SPEED, cornerPitchOffset, targetCornerPitch);
    }


    private void updateWaveMotion(long currentTime) {
        wavePhase += 0.2F + random.nextFloat() * 0.1F;
        if (wavePhase > Math.PI * 2) {
            wavePhase -= (float) (Math.PI * 2);

            waveAmplitudeYaw = randomLerp(3.0F, 8.0F);
            waveAmplitudePitch = randomLerp(1.5F, 5.0F);
        }

        if (waveAmplitudeYaw == 0.0F) {
            waveAmplitudeYaw = randomLerp(3.0F, 8.0F);
            waveAmplitudePitch = randomLerp(1.5F, 5.0F);
        }
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(random.nextFloat(), min, max);
    }

    @Override
    public Vec3d randomValue() {
        return new Vec3d(0.1, 0.15, 0.1);
    }

    public void reset() {
        currentCorner = 0;
        ticksInCorner = 0;
        cornerDuration = 0;
        cornerYawOffset = 0.0F;
        cornerPitchOffset = 0.0F;
        targetCornerYaw = 0.0F;
        targetCornerPitch = 0.0F;
        lastCornerSwitch = 0L;
        wavePhase = 0.0F;
        waveAmplitudeYaw = 0.0F;
        waveAmplitudePitch = 0.0F;
        currentSpikeYaw = 0.0F;
        currentSpikePitch = 0.0F;
        spikeCooldown = 0;
        shakeYaw = 0.0F;
        shakePitch = 0.0F;
    }

}
