package ru.cookiedlc.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.common.util.entity.MovingUtil;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.event.events.player.MoveEvent;
import ru.cookiedlc.event.events.player.TickEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import ru.cookiedlc.module.impl.misc.ElytraHelper;
import ru.cookiedlc.module.impl.misc.autoduel.TimerUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@Slf4j
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Fly extends Module {
    public static Fly getInstance() {
        return Instance.get(Fly.class);
    }

    SelectSetting mode = new SelectSetting("Режим", "Выберите режим полета")
            .value("Ванилла", "Драгон флай" , "Дамаг флай", "Грим элитра", "Телепорт")
            .selected("Ванилла");

    ValueSetting speedXZ = new ValueSetting("Скорость XZ", "Горизонтальная скорость")
            .setValue(1.5F).range(1.0F, 10.0F)
            .visible(() -> !mode.isSelected("Грим элитра") && !mode.isSelected("Телепорт"));
    ValueSetting speedY = new ValueSetting("Скорость Y", "Вертикальная скорость")
            .setValue(1.5F).range(0.0F, 10.0F)
            .visible(() -> !mode.isSelected("Грим элитра") && !mode.isSelected("Телепорт"));


    @NonFinal
    StopWatch timer = new StopWatch();
    @NonFinal
    int teleportCooldown = 0;
    @NonFinal
    Vec3d LastTeleportPos = Vec3d.ZERO;

    final List<Packet<?>> blinkPackets = new CopyOnWriteArrayList<>();
    @NonFinal
    long blinkLastPulseTime = -1;

    static int ticks;
    private static int oldSlot;

    public Fly() {
        super("Fly", ModuleCategory.MOVEMENT);
        setup(mode, speedXZ, speedY);
    }

    TimerUtil t = new TimerUtil();


    @EventHandler
    public void onTick(TickEvent e) {
        if (!state || mc.player == null || mc.world == null) return;

        if (mode.isSelected("Ванилла")) {
            double motionY = getMotionY();
            setMotion(speedXZ.getValue());
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, motionY, v.z);
        } else if (mode.isSelected("Драгон флай")) {
            if (mc.player.getAbilities().flying) {
                setMotion(speedXZ.getValue());
                double motionY = 0.0;
                if (mc.options.jumpKey.isPressed()) {
                    motionY = speedY.getValue();
                }
                if (mc.options.sneakKey.isPressed()) {
                    motionY = -speedY.getValue();
                }
                Vec3d v = mc.player.getVelocity();
                mc.player.setVelocity(v.x, motionY, v.z);
            }
        } else if (mode.isSelected("Холиворлд")) {
            this.ticks = 0;
            while (this.ticks < 9) {
                if (mc.player.getInventory().getStack(this.ticks).isOf(Items.ELYTRA) && !mc.player.isOnGround() && !mc.player.isTouchingWater()) {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, this.ticks, SlotActionType.SWAP, mc.player);
                    if (mc.player.isOnGround()) mc.player.jump();
                    if (!mc.player.isGliding()) {
                        if (t.hasTimeElapsed((long) MathUtil.getRandom(89, 110))) {
                            mc.player.startGliding();
                            t.resetCounter();
                        }
                    }
                    if (!mc.player.isOnGround() && mc.player.isGliding())
                        mc.player.setVelocity(0, mc.player.getVelocity().y + 0.049D, 0);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, this.ticks, SlotActionType.SWAP, mc.player);
                    this.oldSlot = this.ticks;
                }
                ++this.ticks;
            }
        } else if (mode.isSelected("Damage Fly")) {
            if (mc.player.fallDistance > 3) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
                for (int i1 = 0; i1 < 60; i1++) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), true, false));
                    for (int i = 0; i < 3; i++) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY() + 1e-10, mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), false, false));
                    }
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), true, false));
                }
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), mc.player.getPitch(), true, false));
                mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.OFF_HAND));
                if (!mc.player.isSprinting())
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                mc.player.fallDistance = 0;
            }
        } else if (mode.isSelected("Телепорт")) {
            blinkFlushUpdate();
        } else if (mode.isSelected("Грим элитра")) {
            ItemStack chestplate = mc.player.getEquippedStack(EquipmentSlot.CHEST);
            ElytraHelper elytraHelper = Instance.get(ElytraHelper.class);
            if (elytraHelper != null) {
                elytraHelper.switchState();
            }
            if (!chestplate.isOf(Items.ELYTRA)) {
                logDirect(Formatting.RED + "Сначала надень элитру потом включай модуль");
                setState(false);
                return;
            }

            if (mc.player.isOnGround() || mc.player.isTouchingWater()) {
                mc.player.jump();
                t.resetCounter();
            }

            if (!mc.player.isOnGround() && !mc.player.isTouchingWater() && !mc.player.isInLava()) {
                if (!mc.player.isGliding()) {
                    if (t.hasTimeElapsed(50)) {
                        mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                        mc.player.startGliding();
                        t.resetCounter();
                    }
                } else {
                    Vec3d velocity = mc.player.getVelocity();
                    mc.player.setPitch((float) MathUtil.getRandom(-.1f, 2.1f));
                    mc.player.setVelocity(0, velocity.y + 0.03D, 0);
                }
            }
        }
    }


    @EventHandler
    public void onPacket(PacketEvent e) {
        if (!state || mc.player == null || !mode.isSelected("Телепорт")) return;
        if (e.isSend()) {
            blinkPackets.add(e.getPacket());
            e.cancel();
        }
    }

    private void blinkFlushUpdate() {
        if (PlayerIntersectionUtil.nullCheck()) return;
        long now = System.currentTimeMillis();
        if (blinkLastPulseTime == -1) {
            blinkLastPulseTime = now;
            return;
        }
        if (now - blinkLastPulseTime >= 400L) {
            blinkPackets.forEach(PlayerIntersectionUtil::sendPacketWithOutEvent);
            blinkPackets.clear();
            blinkLastPulseTime = now;
        }
    }

    @EventHandler
    public void onMove(MoveEvent e) {
        if (mc.player == null || mc.world == null) return;
        if (!mode.isSelected("Телепорт")) return;
        testMoveEvent(e);
    }

    public void testMoveEvent(MoveEvent e) {
        if (teleportCooldown > 0) {
            teleportCooldown--;
            e.setMovement(Vec3d.ZERO);
            return;
        }

        Vec3d direction = calculateTeleportDirection();
        if (direction.lengthSquared() > 0.001) {
            float distance = 1;
            Vec3d targetPos = mc.player.getPos().add(direction.multiply(distance));

            teleport(targetPos);
            teleportCooldown = 1;
            e.setMovement(Vec3d.ZERO);
        }
    }

    private Vec3d calculateTeleportDirection() {
        float forward = mc.options.forwardKey.isPressed() ? 1 : 0;
        forward -= mc.options.backKey.isPressed() ? 1 : 0;

        float strafe = mc.options.leftKey.isPressed() ? 1 : 0;
        strafe -= mc.options.rightKey.isPressed() ? 1 : 0;

        float vertical = mc.options.jumpKey.isPressed() ? 1f : mc.options.sneakKey.isPressed() ? -3f : 0f;
        if (Math.abs(forward) < 0.01f && Math.abs(strafe) < 0.01f && Math.abs(vertical) < 0.01f) {
            return Vec3d.ZERO;
        }
        float yaw = mc.player.getYaw();
        double yawRad = Math.toRadians(yaw);

        double motionX = strafe * Math.cos(yawRad) - forward * Math.sin(yawRad);
        double motionZ = forward * Math.cos(yawRad) + strafe * Math.sin(yawRad);

        Vec3d horizontalDirection = new Vec3d(motionX, 0, motionZ);
        if (horizontalDirection.lengthSquared() > 0.01) {
            horizontalDirection = horizontalDirection.normalize();
        }
        Vec3d dir = new Vec3d(horizontalDirection.x, vertical, horizontalDirection.z);
        return dir.lengthSquared() > 0.001 ? dir.normalize() : Vec3d.ZERO;
    }


    private void teleport(Vec3d destination) {
        mc.player.setVelocity(Vec3d.ZERO);
        LastTeleportPos = destination;
        mc.player.setPosition(destination.x, destination.y, destination.z);

        sendTeleportPackets(destination);
    }

    private void sendTeleportPackets(Vec3d pos) {
        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;

        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, true, false));
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, true, true));
    }


    private double getMotionY() {
        if (mc.options.sneakKey.isPressed()) {
            return -speedY.getValue();
        } else if (mc.options.jumpKey.isPressed()) {
            return speedY.getValue();
        }
        return 0.0;
    }

    private void setMotion(float speed) {
        float yaw = mc.player.getYaw();
        float f = mc.player.forwardSpeed;
        float s = mc.player.sidewaysSpeed;
        float speedScale = speed / 3.0F;
        double x = 0.0;
        double z = 0.0;
        if (f != 0.0F || s != 0.0F) {
            float yawRad = yaw * ((float)Math.PI / 180.0F);
            x = -MathHelper.sin(yawRad) * speedScale * f + MathHelper.cos(yawRad) * speedScale * s;
            z = MathHelper.cos(yawRad) * speedScale * f + MathHelper.sin(yawRad) * speedScale * s;
        }
        mc.player.setVelocity(x, mc.player.getVelocity().y, z);
    }

    @Override
    public void activate() {
        ticks = 0;
        if (mode.isSelected("Грим элитра")) {
            logDirect("Для корректной работы флая зайдите с via version 1.17");
        }
        super.activate();
    }

    @Override
    public void deactivate() {
        if (mode.isSelected("Телепорт")) {
            if (!blinkPackets.isEmpty()) {
                blinkPackets.forEach(PlayerIntersectionUtil::sendPacketWithOutEvent);
                blinkPackets.clear();
            }
            blinkLastPulseTime = -1;
        }
        if (mc.player != null && mode.isSelected("Телепорт")) {
            mc.player.getAbilities().flying = false;
        }
        super.deactivate();
        timer.reset();
    }
}

