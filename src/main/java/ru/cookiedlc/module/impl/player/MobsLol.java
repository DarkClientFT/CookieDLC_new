package ru.cookiedlc.module.impl.player;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.GroupSetting;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.module.impl.combat.KillAura;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class MobsLol extends Module {

    final GroupSetting mobGroup = new GroupSetting("Mob Settings", "Настройки моба").setValue(true);

    final SelectSetting mobType = new SelectSetting("Mob Type", "Выбор типа моба")
            .value("Pig", "Cow", "Sheep", "Chicken", "Wolf", "Cat", "Fox", "Rabbit")
            .selected("Pig");

    final BooleanSetting glowing = new BooleanSetting("Glowing", "Подсветка моба")
            .setValue(true);

    final BooleanSetting silent = new BooleanSetting("Silent", "Бесшумный моб")
            .setValue(true);

    final GroupSetting followGroup = new GroupSetting("Follow Settings", "Настройки следования").setValue(true);

    final ValueSetting followDistance = new ValueSetting("Follow Distance", "Дистанция следования за игроком")
            .setValue(2.5f).range(1f, 6f);

    final ValueSetting moveSpeed = new ValueSetting("Move Speed", "Скорость передвижения")
            .setValue(0.15f).range(0.05f, 0.4f);

    final GroupSetting attackGroup = new GroupSetting("Attack Settings", "Настройки атаки").setValue(true);

    final BooleanSetting attackAuraTarget = new BooleanSetting("Attack Aura Target", "Атаковать цель KillAura")
            .setValue(true);

    final ValueSetting attackSpeed = new ValueSetting("Attack Speed", "Скорость атаки моба")
            .setValue(0.25f).range(0.1f, 0.5f);

    final ValueSetting attackDistance = new ValueSetting("Attack Distance", "Дистанция начала атаки")
            .setValue(4f).range(2f, 8f);

    AnimalEntity mobEntity;
    Vec3d targetPosition;
    boolean addedToWorld = false;
    int tickCounter = 0;
    String currentMobType = "";
    LivingEntity currentAttackTarget = null;

    public MobsLol() {
        super("Mobs", "Mobs", ModuleCategory.PLAYER);
        mobGroup.settings(mobType, glowing, silent);
        followGroup.settings(followDistance, moveSpeed);
        attackGroup.settings(attackAuraTarget, attackSpeed, attackDistance);
        setup(mobGroup, followGroup, attackGroup);
    }

    @Override
    public void activate() {
        super.activate();
        currentMobType = mobType.getSelected();
        createMob();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        removeMob();
    }

    private void createMob() {
        if (mc.world == null || mc.player == null) return;

        mobEntity = (AnimalEntity) createMobByType(mobType.getSelected());
        if (mobEntity == null) return;

        Vec3d playerPos = mc.player.getPos();
        Vec3d spawnPos = playerPos.add(-followDistance.getValue(), 0, 0);

        mobEntity.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        mobEntity.setNoGravity(false);
        mobEntity.setSilent(silent.isValue());
        mobEntity.setInvulnerable(true);
        mobEntity.setAiDisabled(true);
        mobEntity.setInvisible(false);
        mobEntity.setGlowing(glowing.isValue());

        try {
            mc.world.addEntity(mobEntity);
            addedToWorld = true;
            currentMobType = mobType.getSelected();
        } catch (Exception e) {
            addedToWorld = false;
        }
    }

    private AnimalEntity createMobByType(String type) {
        if (mc.world == null) return null;
        return switch (type) {
            case "Pig" -> new PigEntity(EntityType.PIG, mc.world);
            case "Cow" -> new CowEntity(EntityType.COW, mc.world);
            case "Sheep" -> new SheepEntity(EntityType.SHEEP, mc.world);
            case "Chicken" -> new ChickenEntity(EntityType.CHICKEN, mc.world);
            case "Wolf" -> new WolfEntity(EntityType.WOLF, mc.world);
            case "Cat" -> new CatEntity(EntityType.CAT, mc.world);
            case "Fox" -> new FoxEntity(EntityType.FOX, mc.world);
            case "Rabbit" -> new RabbitEntity(EntityType.RABBIT, mc.world);
            default -> new PigEntity(EntityType.PIG, mc.world);
        };
    }

    private void removeMob() {
        if (mobEntity != null && mc.world != null) {
            try {
                if (addedToWorld) {
                    mobEntity.remove(net.minecraft.entity.Entity.RemovalReason.DISCARDED);
                    mc.world.removeEntity(mobEntity.getId(), net.minecraft.entity.Entity.RemovalReason.DISCARDED);
                }
            } catch (Exception ignored) {}
            addedToWorld = false;
        }
        mobEntity = null;
        targetPosition = null;
        currentAttackTarget = null;
        tickCounter = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (!mobType.getSelected().equals(currentMobType)) {
            removeMob();
            createMob();
            return;
        }

        if (mobEntity == null || mobEntity.isRemoved()) {
            createMob();
            return;
        }

        tickCounter++;

        updateMobSettings();

        LivingEntity auraTarget = getAuraTarget();

        if (attackAuraTarget.isValue() && auraTarget != null && auraTarget.isAlive()) {
            currentAttackTarget = auraTarget;
            attackTarget(auraTarget);
        } else {
            currentAttackTarget = null;
            followPlayer();
        }

        if (tickCounter % 10 == 0) {
            mobEntity.setGlowing(glowing.isValue());
            mobEntity.setSilent(silent.isValue());
        }
    }

    private LivingEntity getAuraTarget() {
        KillAura killAura = KillAura.getInstance();
        if (killAura != null && killAura.isState()) {
            return killAura.getTarget();
        }
        return null;
    }

    private void updateMobSettings() {
        if (mobEntity == null) return;
        mobEntity.setGlowing(glowing.isValue());
        mobEntity.setSilent(silent.isValue());
    }

    private void followPlayer() {
        if (mc.player == null || mobEntity == null) return;

        Vec3d playerPos = mc.player.getPos();
        Vec3d playerLook = mc.player.getRotationVec(1.0f);

        targetPosition = playerPos.subtract(playerLook.multiply(followDistance.getValue()));

        moveToTarget(targetPosition, moveSpeed.getValue(), true);
    }

    private void attackTarget(LivingEntity target) {
        if (target == null || mobEntity == null) return;

        Vec3d targetPos = target.getPos();
        Vec3d mobPos = mobEntity.getPos();
        double distance = mobPos.distanceTo(targetPos);

        if (distance > 1.0) {
            moveToTarget(targetPos, attackSpeed.getValue(), false);
        }

        if (distance <= attackDistance.getValue()) {
            moveToTarget(targetPos, attackSpeed.getValue() * 1.5f, false);

            if (tickCounter % 15 == 0 && distance < 2.0) {
                Vec3d jumpVel = targetPos.subtract(mobPos).normalize().multiply(0.3);
                mobEntity.setVelocity(jumpVel.x, 0.2, jumpVel.z);
            }
        }

        lookAt(targetPos);
    }

    private void moveToTarget(Vec3d target, float speed, boolean lookAtPlayer) {
        if (mobEntity == null || target == null) return;

        Vec3d currentPos = mobEntity.getPos();
        Vec3d direction = target.subtract(currentPos);
        double distance = direction.length();

        if (distance > 0.5) {
            Vec3d movement = direction.normalize().multiply(speed);
            Vec3d newPos = currentPos.add(movement);

            double groundY = findGroundLevel(newPos.x, newPos.z);

            mobEntity.setPos(newPos.x, groundY, newPos.z);

            float bodyYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            mobEntity.setYaw(bodyYaw);

            mobEntity.limbAnimator.setSpeed(2.0f);
        } else {
            mobEntity.limbAnimator.setSpeed(0.0f);
        }

        if (lookAtPlayer && mc.player != null) {
            lookAt(mc.player.getPos());
        } else if (currentAttackTarget != null) {
            lookAt(currentAttackTarget.getPos());
        }

        mobEntity.updatePosition(mobEntity.getX(), mobEntity.getY(), mobEntity.getZ());
    }

    private void lookAt(Vec3d target) {
        if (mobEntity == null || target == null) return;

        Vec3d lookDir = target.subtract(mobEntity.getPos());
        float headYaw = (float) Math.toDegrees(Math.atan2(-lookDir.x, lookDir.z));
        mobEntity.setHeadYaw(headYaw);
    }

    private double findGroundLevel(double x, double z) {
        if (mc.world == null || mc.player == null) return mc.player.getY();

        double startY = mc.player.getY() + 5;

        for (int y = (int) startY; y > mc.world.getBottomY(); y--) {
            BlockPos pos = new BlockPos((int) x, y, (int) z);
            if (mc.world.getBlockState(pos).isSolidBlock(mc.world, pos)) {
                return y + 1.0;
            }
        }

        return mc.player.getY();
    }
}