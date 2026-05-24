package ru.cookiedlc.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.input.Input;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationController;

import java.util.Objects;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Jesus extends Module {

    SelectSetting mode = new SelectSetting("Режим", "Выберите режим передвижения по воде")
            .value("Matrix", "MetaHVH", "FunTime New")
            .selected("Matrix");

    ValueSetting funtimeSpeed = new ValueSetting("Скорость FT", "Скорость передвижения по воде")
            .range(0.01f, 0.2f)
            .setValue(0.08f)
            .visible(() -> mode.isSelected("FunTime New"));

    StopWatch timer = new StopWatch();

    @NonFinal
    boolean isMoving;

    @NonFinal
    int tickCounter = 0;

    float melonBallSpeed = 0.44F;

    public Jesus() {
        super("Jesus", ModuleCategory.MOVEMENT);
        setup(mode, funtimeSpeed);
    }

    @Override

    public void deactivate() {
        tickCounter = 0;
    }

    @EventHandler
    public void tick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (mode.isSelected("Matrix")) {
            handleMatrixMode();
        } else if (mode.isSelected("MetaHVH")) {
            handleMetaHVHMode();
        } else if (mode.isSelected("FunTime New")) {
            handleFunTimeNewMode();
        }
    }

    private void handleMatrixMode() {
        if (mc.player.isTouchingWater() || mc.player.isInLava()) {
            StatusEffectInstance speedEffect = mc.player.getStatusEffect(StatusEffects.SPEED);
            StatusEffectInstance slowEffect = mc.player.getStatusEffect(StatusEffects.SLOWNESS);
            ItemStack offHandItem = mc.player.getOffHandStack();

            String itemName = offHandItem.getName().getString();
            float appliedSpeed = 0F;

            if (itemName.contains("Ломтик Дыни") && speedEffect != null && speedEffect.getAmplifier() == 2) {
                appliedSpeed = 0.4283F * 1.15F;
            } else {
                if (speedEffect != null) {
                    if (speedEffect.getAmplifier() == 2) {
                        appliedSpeed = melonBallSpeed * 1.15F;
                    } else if (speedEffect.getAmplifier() == 1) {
                        appliedSpeed = melonBallSpeed;
                    }
                } else {
                    appliedSpeed = melonBallSpeed * 0.68F;
                }
            }

            if (slowEffect != null) {
                appliedSpeed *= 0.85f;
            }

            setVelocity(appliedSpeed);

            isMoving = mc.options.forwardKey.isPressed()
                    || mc.options.backKey.isPressed()
                    || mc.options.leftKey.isPressed()
                    || mc.options.rightKey.isPressed();

            if (!isMoving) {
                mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
            }

            double yMotion = mc.options.jumpKey.isPressed() ? 0.019 : 0.003;
            mc.player.setVelocity(mc.player.getVelocity().x, yMotion, mc.player.getVelocity().z);
        }
    }

    private void handleMetaHVHMode() {
        if (mc.player.isTouchingWater() || mc.player.isInLava()) {
            StatusEffectInstance speedEffect = mc.player.getStatusEffect(StatusEffects.SPEED);
            StatusEffectInstance slowEffect = mc.player.getStatusEffect(StatusEffects.SLOWNESS);

            float appliedSpeed = 0.47F;

            if (speedEffect != null) {
                if (speedEffect.getAmplifier() == 2) {
                    appliedSpeed = 0.47F * 1.2F;
                } else if (speedEffect.getAmplifier() == 1) {
                    appliedSpeed = 0.47F * 1.05F;
                }
            } else {
                appliedSpeed = 0.47F * 0.7F;
            }

            if (slowEffect != null) {
                appliedSpeed *= 0.8f;
            }

            setVelocity(appliedSpeed);

            isMoving = mc.options.forwardKey.isPressed()
                    || mc.options.backKey.isPressed()
                    || mc.options.leftKey.isPressed()
                    || mc.options.rightKey.isPressed();

            if (!isMoving) {
                mc.player.setVelocity(0.0, mc.player.getVelocity().y, 0.0);
            }

            double yMotion = mc.options.jumpKey.isPressed() ? 0.025 : 0.005;
            mc.player.setVelocity(mc.player.getVelocity().x, yMotion, mc.player.getVelocity().z);
        }
    }

    private void handleFunTimeNewMode() {
        if (mc.player.isInFluid() || mc.player.isTouchingWater()) {
            tickCounter++;
            if (tickCounter > 2) tickCounter = 0;

            if (hasPlayerMovement()) {
                double speed = funtimeSpeed.getValue();

                double yaw = Math.toRadians(mc.player.getYaw());
                double motionX = -Math.sin(yaw) * speed;
                double motionZ = Math.cos(yaw) * speed;

                double motionY;
                if (tickCounter == 0) {
                    motionY = 0.05;
                } else if (tickCounter == 2) {
                    motionY = -0.05;
                } else {
                    motionY = 0;
                }

                mc.player.setVelocity(motionX, motionY, motionZ);
            } else {
                mc.player.setVelocity(0, 0, 0);
            }
        } else {
            tickCounter = 0;
        }
    }
    public static void setVelocity(double velocity) {
        final double[] direction = calculateDirection(velocity);
        Objects.requireNonNull(mc.player).setVelocity(direction[0], mc.player.getVelocity().getY(), direction[1]);
    }
    public static boolean hasPlayerMovement() {
        Input input = mc.player.input;
        if (input.hasForwardMovement()) {
            return true;
        }
        Vec2f vec = input.getMovementInput();
        return vec.x != 0f || vec.y != 0f;
    }
    public static double[] calculateDirection(double distance) {
        Vec2f movement = mc.player.input.getMovementInput();
        float forward = movement.y;
        float sideways = movement.x;
        return calculateDirection(forward, sideways, distance);
    }
    public static double[] calculateDirection(float forward, float sideways, double distance) {
        float yaw = RotationController.INSTANCE.getRotation().getYaw();
        if (forward != 0.0f) {
            if (sideways > 0.0f) {
                yaw += (forward > 0.0f) ? -45 : 45;
            } else if (sideways < 0.0f) {
                yaw += (forward > 0.0f) ? 45 : -45;
            }
            sideways = 0.0f;
            forward = (forward > 0.0f) ? 1.0f : -1.0f;
        }

        double sinYaw = Math.sin(Math.toRadians(yaw + 90.0f));
        double cosYaw = Math.cos(Math.toRadians(yaw + 90.0f));
        double xMovement = forward * distance * cosYaw + sideways * distance * sinYaw;
        double zMovement = forward * distance * sinYaw - sideways * distance * cosYaw;

        return new double[]{xMovement, zMovement};
    }

}