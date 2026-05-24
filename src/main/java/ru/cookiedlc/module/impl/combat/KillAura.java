package ru.cookiedlc.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.*;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.api.types.EventType;
import ru.cookiedlc.event.events.player.InputEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.api.system.animation.Animation;
import ru.cookiedlc.api.system.animation.Direction;
import ru.cookiedlc.api.system.animation.implement.DecelerateAnimation;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.common.util.render.Render3DUtil;
import ru.cookiedlc.common.util.task.TaskPriority;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.event.events.player.RotationUpdateEvent;
import ru.cookiedlc.event.events.render.WorldRenderEvent;
import ru.cookiedlc.module.impl.combat.killaura.rotation.modes.snap.*;
import ru.cookiedlc.ui.hud.render.Notifications;
import ru.cookiedlc.module.impl.combat.killaura.attack.AttackHandler;
import ru.cookiedlc.module.impl.combat.killaura.attack.AttackPerpetrator;
import ru.cookiedlc.module.impl.combat.killaura.rotation.*;
import ru.cookiedlc.module.impl.combat.killaura.rotation.angle.AngleSmoothMode;
import ru.cookiedlc.module.impl.combat.killaura.rotation.modes.smooth.*;
import ru.cookiedlc.module.impl.combat.killaura.target.TargetSelector;
import ru.cookiedlc.module.impl.render.Hud;
import ru.cookiedlc.module.api.setting.implement.GroupSetting;
import ru.cookiedlc.module.api.setting.implement.MultiSelectSetting;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class KillAura extends Module {
    public static KillAura getInstance() {
        return Instance.get(KillAura.class);
    }
    Animation esp_anim = new DecelerateAnimation().setMs(400).setValue(1);
    TargetSelector targetSelector = new TargetSelector();
    PointFinder pointFinder = new PointFinder();

    @NonFinal
    LivingEntity target, lastTarget;

    MultiSelectSetting targetType = new MultiSelectSetting("Target Type", "Filters the entire list of targets by type")
            .value("Players", "Mobs", "Animals", "Friends");

    MultiSelectSetting attackSetting = new MultiSelectSetting("Attack Setting", "Allows you to customize the attack")
            .value("Only Critical", "Smart Crits", "Dynamic Cooldown", "Break Shield", "UnPress Shield", "No Attack When Eat", "Ignore The Walls");

    SelectSetting aimMode = new SelectSetting("Rotation Type", "Allows you to select the rotation type")
            .value(
                    "Smooth",
                    "Snap",
                    "Matrix",
                    "HvH",
                    "Legit",
                    "FunTime",
                    "MindAI",
                    "WatchAI",
                    "KallMode",
                    "FTEST"
            ).selected("Smooth");

    SelectSetting correctionType = new SelectSetting("Correction Type", "Selects the type of correction")
            .value("Free", "Focus", "Target").selected("Focused");

    GroupSetting correctionGroup = new GroupSetting("Move correction", "Prevents detection by movement sensitive anti-cheats")
            .settings(correctionType).setValue(true);

    SelectSetting targetEspType = new SelectSetting("Target Esp Type", "Selects the type of target esp")
            .value("Cube", "Circle", "Ghosts").selected("Circle");

    ValueSetting ghostSpeed = new ValueSetting("Ghost Speed", "Speed of ghost flying around the target")
            .setValue(1).range(1F, 2F).visible(() -> targetEspType.isSelected("Ghosts"));

    GroupSetting targetEspGroup = new GroupSetting("Target Esp", "Displays the player in the world")
            .settings(targetEspType, ghostSpeed).setValue(true);

    ValueSetting attackRange = new ValueSetting("Attack Range", "Maximum distance for attacking the target")
            .setValue(3.3F).range(2F, 6F);

    ValueSetting rotationRange = new ValueSetting("Rotation Range", "Maximum distance for rotating to and selecting the target")
            .setValue(3.5F).range(2F, 10F);

    ValueSetting rotationRangeInElytra = new ValueSetting("Rotation Range In Elytra", "Maximum distance for rotating to target while flying with elytra")
            .setValue(6F).range(2F, 50F);

    ValueSetting fovLimit = new ValueSetting("FOV Limit", "Field of view angle limit for target selection (degrees)")
            .setValue(360).range(30F, 360F);

    public KillAura() {
        super("KillAura", "KillAura", ModuleCategory.COMBAT);
        setup(targetType, attackRange, rotationRange, rotationRangeInElytra, attackSetting, fovLimit, correctionGroup, aimMode, targetEspGroup);
    }

    @Override
    public void activate() {
        super.activate();
    }

    @Override
    public void deactivate() {
        targetSelector.releaseTarget();
        target = null;
        RotationController.INSTANCE.clear();
        super.deactivate();
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        esp_anim.setDirection(target != null ? Direction.FORWARDS : Direction.BACKWARDS);
        float anim = esp_anim.getOutput().floatValue();
        if (targetEspGroup.isValue() && lastTarget != null && !esp_anim.isFinished(Direction.BACKWARDS)) {
            float red = MathHelper.clamp((lastTarget.hurtTime - tickCounter.getTickDelta(false)) / 10, 0, 1);
            switch (targetEspType.getSelected()) {
                case "Cube" -> Render3DUtil.drawCube(lastTarget, anim, red);
                case "Circle" -> Render3DUtil.drawCircle(e.getStack(), lastTarget, anim, red);
                case "Ghosts" -> Render3DUtil.drawGhosts(lastTarget, anim, red, ghostSpeed.getValue());
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof EntityStatusS2CPacket status && status.getStatus() == 30) {
            Entity entity = status.getEntity(mc.world);
            if (entity != null && entity.equals(target) && Hud.getInstance().notificationSettings.isSelected("Break Shield")) {
                Notifications.getInstance().addList(Text.literal("Сломали щит игроку - ").append(entity.getDisplayName()), 3000);
            }
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (!isEnabled()) {
            return;
        }

        switch (e.getType()) {
            case EventType.PRE -> {
                target = updateTarget();
                if (target != null) {
                    rotateToTarget(getConfig());
                    lastTarget = target;
                } else {
                }
            }
            case EventType.POST -> {
                Render3DUtil.updateTargetEsp();
                if (target != null) {
                    Main.getInstance().getAttackPerpetrator().performAttack(getConfig());
                }
            }
        }
    }

    private LivingEntity updateTarget() {
        TargetSelector.EntityFilter filter = new TargetSelector.EntityFilter(targetType.getSelected());
        boolean isUsingElytra = mc.player != null && mc.player.isGliding();
        float effectiveRange = isUsingElytra ? rotationRangeInElytra.getValue() : rotationRange.getValue();

        targetSelector.searchTargets(
                mc.world.getEntities(),
                effectiveRange,
                fovLimit.getValue(),
                attackSetting.isSelected("Ignore The Walls")
        );

        targetSelector.validateTarget(filter::isValid);
        return targetSelector.getCurrentTarget();
    }

    private void rotateToTarget(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        AttackHandler attackHandler = Main.getInstance().getAttackPerpetrator().getAttackHandler();
        RotationController controller = RotationController.INSTANCE;
        Angle.VecRotation rotation = new Angle.VecRotation(config.getAngle(), config.getAngle().toVector());
        RotationConfig rotationConfig = getRotationConfig();
        switch (aimMode.getSelected()) {
            case "WatchAI", "Snap" -> {
                if (attackHandler.canAttack(config, 1) || !attackHandler.getAttackTimer().finished(100)) {
                    controller.rotateTo(rotation, target, 3, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                }
            }
            case "KallMode" -> {
                if (attackHandler.canAttack(config, 3)) {
                    controller.clear();
                    controller.rotateTo(rotation, target, 50, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                }
            }
            case "FunTime", "FTEST" -> {
                if (attackHandler.canAttack(config, 5)) {
                    controller.clear();
                    controller.rotateTo(rotation, target, 40, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                }
            }
            case "Vulcan", "Legit", "MindAI", "Matrix", "HvH"  -> {
                controller.rotateTo(rotation, target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
            }
            default -> controller.rotateTo(rotation, target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
        }
    }
    @EventHandler
    public void onInput(InputEvent event) {
        if (mc.player == null || mc.world == null)
            return;

        PlayerInput input = event.getInput();
        if (input == null)
            return;

        if (!isState())
            return;

        if (target == null || !target.isAlive())
            return;

        boolean w = mc.options.forwardKey.isPressed();
        boolean s = mc.options.backKey.isPressed();
        boolean a = mc.options.leftKey.isPressed();
        boolean d = mc.options.rightKey.isPressed();

        if (correctionType.isSelected("Target")) {
            Vec3d playerPos = mc.player.getPos();
            Vec3d targetPos = target.getPos();

            Vec3d moveTarget = new Vec3d(targetPos.x, playerPos.y, targetPos.z);
            Vec3d dir = moveTarget.subtract(playerPos).normalize();

            float yaw = RotationController.INSTANCE.getRotation().getYaw();
            float moveAngle = (float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90F;
            float angleDiff = MathHelper.wrapDegrees(moveAngle - yaw);

            boolean forward = false, back = false, left = false, right = false;

            if (angleDiff >= -22.5 && angleDiff < 22.5) {
                forward = true;
            } else if (angleDiff >= 22.5 && angleDiff < 67.5) {
                forward = true;
                right = true;
            } else if (angleDiff >= 67.5 && angleDiff < 112.5) {
                right = true;
            } else if (angleDiff >= 112.5 && angleDiff < 157.5) {
                back = true;
                right = true;
            } else if (angleDiff >= -67.5 && angleDiff < -22.5) {
                forward = true;
                left = true;
            } else if (angleDiff >= -112.5 && angleDiff < -67.5) {
                left = true;
            } else if (angleDiff >= -157.5 && angleDiff < -112.5) {
                back = true;
                left = true;
            } else {
                back = true;
            }

            event.setDirectional(forward, back, left, right);
            return;
        }

        if (correctionType.isSelected("Focus")) {
            if (!w && !s && !a && !d)
                return;

            Vec3d playerPos = mc.player.getPos();
            Box targetBox = target.getBoundingBox();
            Vec3d center = targetBox.getCenter();

            float targetYaw = target.getYaw();
            double rad = Math.toRadians(targetYaw);

            Vec3d forwardDir = new Vec3d(-Math.sin(rad), 0, Math.cos(rad)).normalize();
            Vec3d rightDir = new Vec3d(-forwardDir.z, 0, forwardDir.x).normalize();
            Vec3d leftDir = rightDir.multiply(-1);

            double halfWidth = target.getWidth() / 2.0;
            double offset = halfWidth + 0.1;

            Vec3d moveTargetVec = center;
            Vec3d offsetVec = Vec3d.ZERO;

            if (w)
                offsetVec = offsetVec.add(forwardDir);
            if (s)
                offsetVec = offsetVec.add(forwardDir.multiply(-1.0));
            if (a)
                offsetVec = offsetVec.add(leftDir);
            if (d)
                offsetVec = offsetVec.add(rightDir);

            if (offsetVec.lengthSquared() > 0) {
                offsetVec = offsetVec.normalize().multiply(offset);
                moveTargetVec = center.add(offsetVec);
            }

            moveTargetVec = new Vec3d(moveTargetVec.x, playerPos.y, moveTargetVec.z);
            Vec3d dir = moveTargetVec.subtract(playerPos).normalize();

            float yaw = RotationController.INSTANCE.getRotation().getYaw();
            float moveAngle = (float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90F;
            float angleDiff = MathHelper.wrapDegrees(moveAngle - yaw);

            boolean forward = false, back = false, left = false, right = false;

            if (angleDiff >= -22.5 && angleDiff < 22.5) {
                forward = true;
            } else if (angleDiff >= 22.5 && angleDiff < 67.5) {
                forward = true;
                right = true;
            } else if (angleDiff >= 67.5 && angleDiff < 112.5) {
                right = true;
            } else if (angleDiff >= 112.5 && angleDiff < 157.5) {
                back = true;
                right = true;
            } else if (angleDiff >= -67.5 && angleDiff < -22.5) {
                forward = true;
                left = true;
            } else if (angleDiff >= -112.5 && angleDiff < -67.5) {
                left = true;
            } else if (angleDiff >= -157.5 && angleDiff < -112.5) {
                back = true;
                left = true;
            } else {
                back = true;
            }

            event.setDirectional(forward, back, left, right);
        }
    }
    public AttackPerpetrator.AttackPerpetratorConfigurable getConfig() {
        Pair<Vec3d, Box> point = pointFinder.computeVector(
                target,
                attackRange.getValue(),
                RotationController.INSTANCE.getRotation(),
                getSmoothMode().randomValue(),
                attackSetting.isSelected("Ignore The Walls")
        );

        Vec3d eyePos = Objects.requireNonNull(mc.player).getEyePos();
        Angle angle = AngleUtil.fromVec3d(point.getLeft().subtract(eyePos));
        Box box = point.getRight();
        List<String> modifiedAttackSettings = new ArrayList<>(attackSetting.getSelected());

        if (attackSetting.isSelected("Smart Crits")) {
            modifiedAttackSettings.remove("Only Critical");
            boolean isOnGround = mc.player.isOnGround();
            boolean isJumping = mc.player.jumping;
            boolean shouldCrit = !isOnGround || isJumping;
            if (shouldCrit) {
                modifiedAttackSettings.add("Only Critical");
            }
        }

        return new AttackPerpetrator.AttackPerpetratorConfigurable(
                target,
                angle,
                attackRange.getValue(),
                modifiedAttackSettings,
                aimMode,
                box
        );
    }

    public RotationConfig getRotationConfig() {
        return new RotationConfig(getSmoothMode(), correctionGroup.isValue(), correctionType.isSelected("Free"), correctionType.isSelected("Targeted"), target);
    }

    public AngleSmoothMode getSmoothMode() {
        return switch (aimMode.getSelected()) {
            case "Matrix" -> new MatrixMode();
            case "HvH" -> new HvhMode();
            case "Legit" -> new LegitMode();
            case "Smooth" -> new SmoothMode();
            case "FunTime" -> new FunTimeMode();
            case "Snap" -> new SnapMode();
            case "MindAI" -> new MindAIMode();
            case "WatchAI" -> new WatchAIMode();
            case "KallMode" -> new KallMode();
            case "FTEST" -> new FTTestMode();
            default -> new LinearMode();
        };
    }
}