package ru.cookiedlc.module.impl.combat.killaura.rotation.modes.smooth;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;
import ru.cookiedlc.module.impl.combat.killaura.rotation.angle.AngleSmoothMode;

import java.util.Random;

public class LegitMode extends AngleSmoothMode {

    private final Random random = new Random();

    private float lastYawDelta = 0f;
    private float lastPitchDelta = 0f;
    private float lastYawAcceleration = 0f;
    private float lastPitchAcceleration = 0f;

    private Vec3d currentTargetPoint = Vec3d.ZERO;
    private Vec3d previousTargetPoint = Vec3d.ZERO;
    private int pointUpdateTicks = 0;
    private int pointUpdateInterval = 5;

    private float lastTotalDelta = 0f;
    private int ticksSinceTargetChange = 0;
    private Entity lastTargetEntity = null;

    private static final float GCD_FACTOR = 0.15f * 0.15f;

    private int microCorrectionCooldown = 0;
    private float accumulatedYawError = 0f;
    private float accumulatedPitchError = 0f;

    private float breathingPhaseYaw = random.nextFloat() * 1000f;
    private float breathingPhasePitch = random.nextFloat() * 1000f;
    private float breathingSpeedYaw = randomFloat(0.04f, 0.09f);
    private float breathingSpeedPitch = randomFloat(0.05f, 0.1f);

    public LegitMode() {
        super("Legit");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3d vec3d, Entity entity) {
        if (entity != lastTargetEntity) {
            lastTargetEntity = entity;
            ticksSinceTargetChange = 0;
            lastYawDelta = 0f;
            lastPitchDelta = 0f;
            lastYawAcceleration = 0f;
            lastPitchAcceleration = 0f;
            accumulatedYawError = 0f;
            accumulatedPitchError = 0f;
            breathingSpeedYaw = randomFloat(0.04f, 0.09f);
            breathingSpeedPitch = randomFloat(0.05f, 0.1f);
        }
        ticksSinceTargetChange++;

        float yawDelta = MathHelper.wrapDegrees(targetAngle.getYaw() - currentAngle.getYaw());
        float pitchDelta = targetAngle.getPitch() - currentAngle.getPitch();

        float totalDelta = (float) Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);

        double distanceToTarget = vec3d != null ? vec3d.length() : 3.0;
        float distanceFactor = calculateDistanceFactor(distanceToTarget);

        float baseMaxYaw, baseMaxPitch;

        if (totalDelta > 90f) {
            baseMaxYaw = randomFloat(45f, 65f);
            baseMaxPitch = randomFloat(25f, 35f);
        } else if (totalDelta > 30f) {
            baseMaxYaw = randomFloat(25f, 40f);
            baseMaxPitch = randomFloat(15f, 25f);
        } else if (totalDelta > 5f) {
            baseMaxYaw = randomFloat(12f, 22f);
            baseMaxPitch = randomFloat(8f, 15f);
        } else {
            baseMaxYaw = randomFloat(3f, 8f);
            baseMaxPitch = randomFloat(2f, 6f);
        }

        float maxYaw = baseMaxYaw * distanceFactor;
        float maxPitch = baseMaxPitch * distanceFactor;

        float accelerationFactor = calculateAcceleration(totalDelta);

        float clampedYaw = clampAbs(yawDelta, maxYaw) * accelerationFactor;
        float clampedPitch = clampAbs(pitchDelta, maxPitch) * accelerationFactor;

        clampedYaw = applyAxisCoupling(clampedYaw, clampedPitch, true);
        clampedPitch = applyAxisCoupling(clampedPitch, clampedYaw, false);

        float inertiaFactor = calculateInertiaFactor(totalDelta);

        float smoothedYaw = lerp(lastYawDelta, clampedYaw, inertiaFactor);
        float smoothedPitch = lerp(lastPitchDelta, clampedPitch, inertiaFactor);

        breathingPhaseYaw += breathingSpeedYaw + randomFloat(-0.005f, 0.005f);
        breathingPhasePitch += breathingSpeedPitch + randomFloat(-0.005f, 0.005f);

        float breathingAmplitude = totalDelta < 15f ? randomFloat(0.08f, 0.18f) : randomFloat(0.03f, 0.08f);
        smoothedYaw += calculateBreathingOffset(breathingPhaseYaw, breathingAmplitude, false);
        smoothedPitch += calculateBreathingOffset(breathingPhasePitch, breathingAmplitude * 1.2f, true);

        if (totalDelta > 1.5f && totalDelta < 60f && random.nextFloat() > 0.6f) {
            smoothedYaw += generateHumanJitter(0.05f, 0.15f);
            smoothedPitch += generateHumanJitter(0.03f, 0.1f);
        }

        if (microCorrectionCooldown <= 0 && totalDelta < 3f && totalDelta > 0.5f) {
            float overshootChance = random.nextFloat();
            if (overshootChance < 0.15f) {
                float overshootMult = randomFloat(1.05f, 1.2f);
                smoothedYaw *= overshootMult;
                smoothedPitch *= overshootMult;
                microCorrectionCooldown = random.nextInt(8) + 5;
            } else if (overshootChance < 0.25f) {
                float undershootMult = randomFloat(0.85f, 0.95f);
                smoothedYaw *= undershootMult;
                smoothedPitch *= undershootMult;
                microCorrectionCooldown = random.nextInt(6) + 3;
            }
        }
        if (microCorrectionCooldown > 0) microCorrectionCooldown--;

        accumulatedYawError += (yawDelta - smoothedYaw) * 0.1f;
        accumulatedPitchError += (pitchDelta - smoothedPitch) * 0.1f;

        if (Math.abs(accumulatedYawError) > 1.5f) {
            smoothedYaw += accumulatedYawError * randomFloat(0.2f, 0.4f);
            accumulatedYawError *= 0.5f;
        }
        if (Math.abs(accumulatedPitchError) > 1.0f) {
            smoothedPitch += accumulatedPitchError * randomFloat(0.2f, 0.4f);
            accumulatedPitchError *= 0.5f;
        }

        float currentYawAcceleration = smoothedYaw - lastYawDelta;
        float currentPitchAcceleration = smoothedPitch - lastPitchDelta;
        lastYawAcceleration = currentYawAcceleration;
        lastPitchAcceleration = currentPitchAcceleration;
        lastYawDelta = smoothedYaw;
        lastPitchDelta = smoothedPitch;
        lastTotalDelta = totalDelta;

        float newYaw = currentAngle.getYaw() + smoothedYaw;
        float newPitch = MathHelper.clamp(currentAngle.getPitch() + smoothedPitch, -90f, 90f);

        newYaw = applyGCDFix(currentAngle.getYaw(), newYaw);
        newPitch = applyGCDFix(currentAngle.getPitch(), newPitch);
        newPitch = MathHelper.clamp(newPitch, -90f, 90f);

        return new Angle(newYaw, newPitch);
    }

    @Override
    public Vec3d randomValue() {
        pointUpdateTicks++;

        if (pointUpdateTicks >= pointUpdateInterval) {
            pointUpdateTicks = 0;
            pointUpdateInterval = random.nextInt(5) + 3;

            previousTargetPoint = currentTargetPoint;
            Vec3d newPoint;
            int attempts = 0;

            do {
                float yBias = random.nextFloat() < 0.7f
                        ? randomFloat(0.1f, 0.5f)
                        : randomFloat(-0.1f, 0.7f);

                newPoint = new Vec3d(
                        gaussianRandom(0f, 0.15f),
                        yBias,
                        gaussianRandom(0f, 0.15f)
                );
                attempts++;
            } while (newPoint.distanceTo(previousTargetPoint) < 0.1 && attempts < 15);

            currentTargetPoint = newPoint;
        }

        float progress = (float) pointUpdateTicks / (float) pointUpdateInterval;
        return lerpVec3dSmooth(previousTargetPoint, currentTargetPoint, progress);
    }


    private float calculateBreathingOffset(float phase, float amplitude, boolean isPitch) {
        float wave1 = (float) Math.sin(phase);
        float wave2 = (float) Math.sin(phase * 1.7f + (isPitch ? 0.5f : 1.2f)) * 0.4f;
        float wave3 = (float) Math.sin(phase * 3.2f + (isPitch ? 2.1f : 0.8f)) * 0.15f;

        return (wave1 + wave2 + wave3) * amplitude;
    }

    private float calculateDistanceFactor(double distance) {
        if (distance < 1.5) return randomFloat(1.3f, 1.5f);
        if (distance < 3.0) return randomFloat(1.0f, 1.2f);
        if (distance < 5.0) return randomFloat(0.85f, 1.0f);
        return randomFloat(0.7f, 0.9f);
    }

    private float calculateAcceleration(float totalDelta) {
        if (ticksSinceTargetChange <= 2) {
            return randomFloat(0.4f, 0.7f);
        }
        if (ticksSinceTargetChange <= 5) {
            return randomFloat(0.7f, 0.95f);
        }
        return randomFloat(0.9f, 1.1f);
    }

    private float calculateInertiaFactor(float totalDelta) {
        if (totalDelta > 45f) {
            return randomFloat(0.55f, 0.75f);
        } else if (totalDelta > 10f) {
            return randomFloat(0.65f, 0.85f);
        } else {
            return randomFloat(0.75f, 0.95f);
        }
    }

    private float applyAxisCoupling(float primaryDelta, float secondaryDelta, boolean isYaw) {
        if (Math.abs(primaryDelta) < 0.05f && Math.abs(secondaryDelta) > 2f) {
            float coupling = secondaryDelta * randomFloat(0.01f, 0.04f);
            coupling *= (random.nextBoolean() ? 1f : -1f);
            return primaryDelta + coupling;
        }
        return primaryDelta;
    }

    private float generateHumanJitter(float minAmplitude, float maxAmplitude) {
        float amplitude = randomFloat(minAmplitude, maxAmplitude);
        return (float) (random.nextGaussian() * amplitude * 0.3f);
    }

    private float applyGCDFix(float currentAngle, float newAngle) {
        float delta = newAngle - currentAngle;
        float gcd = calculateGCD();
        delta -= delta % gcd;
        return currentAngle + delta;
    }

    private float calculateGCD() {
        float sensitivity = 0.5f;
        float f = sensitivity * 0.6f + 0.2f;
        return f * f * f * 1.2f;
    }

    private float gaussianRandom(float mean, float stdDev) {
        return (float) (mean + random.nextGaussian() * stdDev);
    }

    private Vec3d lerpVec3dSmooth(Vec3d start, Vec3d end, float progress) {
        float t = hermiteInterpolation(progress);
        return new Vec3d(
                lerp((float) start.x, (float) end.x, t),
                lerp((float) start.y, (float) end.y, t),
                lerp((float) start.z, (float) end.z, t)
        );
    }

    private float hermiteInterpolation(float t) {
        return t * t * (3f - 2f * t);
    }

    private float randomFloat(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    private float clampAbs(float value, float maxAbs) {
        return Math.signum(value) * Math.min(Math.abs(value), maxAbs);
    }

    private float lerp(float start, float end, float factor) {
        return start + (end - start) * factor;
    }
}