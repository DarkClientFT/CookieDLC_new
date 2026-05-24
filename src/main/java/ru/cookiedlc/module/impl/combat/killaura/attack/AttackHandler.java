package ru.cookiedlc.module.impl.combat.killaura.attack;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.event.api.types.EventType;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.common.util.entity.*;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.core.listener.impl.EventListener;
import ru.cookiedlc.event.events.item.UsingItemEvent;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.module.impl.combat.Criticals;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;
import ru.cookiedlc.module.impl.combat.killaura.rotation.AngleUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RaytracingUtil;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationController;
import ru.cookiedlc.module.impl.movement.AutoSprint;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttackHandler implements QuickImports {
    private final StopWatch attackTimer = new StopWatch(), shieldWatch = new StopWatch();
    private final ClickScheduler clickScheduler = new ClickScheduler();
    private int count = 0;

    void tick() {}

    void onPacket(PacketEvent e) {
        Packet<?> packet = e.getPacket();
        if (packet instanceof HandSwingC2SPacket || packet instanceof UpdateSelectedSlotC2SPacket) {
            clickScheduler.recalculate();
        }
    }

    void onUsingItem(UsingItemEvent e) {
        if (e.getType() == EventType.START && !shieldWatch.finished(50)) {
            e.cancel();
        }
    }

    void handleAttack(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (!isInAttackRange(config)) {
            return;
        }
        boolean canAttackSoon = canAttack(config, 2);
        boolean canAttackNow = canAttack(config, 0);

        if (canAttackSoon) preAttackEntity(config);
        if (RaytracingUtil.rayTrace(config) && canAttackNow && !isSprinting()) {
            attackEntity(config);
        }
    }
    private boolean isInAttackRange(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (mc.player == null || config.getTarget() == null) {
            return false;
        }

        Box targetBox = config.getTarget().getBoundingBox();
        Vec3d eyePos = mc.player.getEyePos();

        double closestX = MathHelper.clamp(eyePos.x, targetBox.minX, targetBox.maxX);
        double closestY = MathHelper.clamp(eyePos.y, targetBox.minY, targetBox.maxY);
        double closestZ = MathHelper.clamp(eyePos.z, targetBox.minZ, targetBox.maxZ);

        double distance = eyePos.distanceTo(new Vec3d(closestX, closestY, closestZ));

        return distance <= config.getMaximumRange();
    }

    void preAttackEntity(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        if (config.isShouldUnPressShield() && mc.player.isUsingItem() && mc.player.getActiveItem().getItem().equals(Items.SHIELD)) {
            mc.interactionManager.stopUsingItem(mc.player);
            shieldWatch.reset();
        }

        if (!mc.player.isSwimming()) {
            AutoSprint.getInstance().tickStop = MathUtil.getRandom(1, 2);
            mc.options.sprintKey.setPressed(false);
        }
    }

    void attackEntity(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        attack(config);
        breakShield(config);
        attackTimer.reset();
        count++;
    }

    private void breakShield(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        LivingEntity target = config.getTarget();
        Angle angleToPlayer = AngleUtil.fromVec3d(mc.player.getBoundingBox().getCenter().subtract(target.getEyePos()));
        boolean targetOnShield = target.isUsingItem() && target.getActiveItem().getItem().equals(Items.SHIELD);
        boolean angle = Math.abs(RotationController.computeAngleDifference(target.getYaw(), angleToPlayer.getYaw())) < 90;
        Slot axe = PlayerInventoryUtil.getSlot(s -> s.getStack().getItem() instanceof AxeItem);

        if (config.isShouldBreakShield() && targetOnShield && axe != null && angle && PlayerInventoryComponent.script.isFinished()) {
            PlayerInventoryUtil.swapHand(axe, Hand.MAIN_HAND, false);
            PlayerInventoryUtil.closeScreen(true);
            attack(config);
            PlayerInventoryUtil.swapHand(axe, Hand.MAIN_HAND, false, true);
            PlayerInventoryUtil.closeScreen(true);
        }
    }

    private void attack(AttackPerpetrator.AttackPerpetratorConfigurable config) {
        mc.interactionManager.attackEntity(mc.player, config.getTarget());
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private boolean isSprinting() {
        return EventListener.serverSprint && !mc.player.isGliding() && !mc.player.isTouchingWater();
    }

    public boolean canAttack(AttackPerpetrator.AttackPerpetratorConfigurable config, int ticks) {
        if (!isInAttackRange(config)) {
            return false;
        }

        for (int i = 0; i <= ticks; i++) {
            if (canCrit(config, i)) {
                return true;
            }
        }
        return false;
    }

    public boolean canCrit(AttackPerpetrator.AttackPerpetratorConfigurable config, int ticks) {
        if (mc.player.isUsingItem() && !mc.player.getActiveItem().getItem().equals(Items.SHIELD) && config.isEatAndAttack()) {
            return false;
        }

        if (!clickScheduler.isCooldownComplete(config.isUseDynamicCooldown(), ticks)) {
            return false;
        }

        SimulatedPlayer simulated = SimulatedPlayer.simulateLocalPlayer(ticks);
        if (config.isOnlyCritical() && !hasMovementRestrictions(simulated)) {
            return isPlayerInCriticalState(simulated, ticks);
        }

        return true;
    }

    private boolean hasMovementRestrictions(SimulatedPlayer simulated) {
        return simulated.hasStatusEffect(StatusEffects.BLINDNESS)
                || simulated.hasStatusEffect(StatusEffects.LEVITATION)
                || PlayerIntersectionUtil.isBoxInBlock(simulated.boundingBox.expand(-1e-3), Blocks.COBWEB)
                || simulated.isSubmergedInWater()
                || simulated.isInLava()
                || simulated.isClimbing()
                || !PlayerIntersectionUtil.canChangeIntoPose(EntityPose.STANDING, simulated.pos)
                || simulated.player.getAbilities().flying;
    }

    private boolean isPlayerInCriticalState(SimulatedPlayer simulated, int ticks) {
        boolean fall = simulated.fallDistance > 0 && (simulated.fallDistance < 0.08 || !SimulatedPlayer.simulateLocalPlayer(ticks + 1).onGround);
        return !simulated.onGround && (fall || Criticals.getInstance().isState());
    }
}