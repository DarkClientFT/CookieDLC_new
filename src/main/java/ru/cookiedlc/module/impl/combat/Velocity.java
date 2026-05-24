package ru.cookiedlc.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.CommonPingS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.event.events.player.TickEvent;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Velocity extends Module {
    public static Velocity getInstance() {
        return Instance.get(Velocity.class);
    }

    SelectSetting mode = new SelectSetting("Режим", "Выберите режим уменьшения отдачи")
            .value("NewGrim", "OldGrim", "Matrix", "Normal")
            .selected("NewGrim");

    @NonFinal boolean flag;
    @NonFinal int grimTicks;
    @NonFinal int ccCooldown;
    @NonFinal Vec3d pendingVelocity;

    public Velocity() {
        super("Velocity", ModuleCategory.COMBAT);
        setup(mode);
    }

    @EventHandler
    
    public void onPacket(PacketEvent e) {
        if (!state) return;
        if (e.getType() != PacketEvent.Type.RECEIVE) return;
        if (mc.player == null || mc.player.isTouchingWater() || mc.player.isSubmergedInWater() || mc.player.isInLava()) return;
        if (ccCooldown > 0) {
            ccCooldown--;
            return;
        }

        if (e.getPacket() instanceof EntityVelocityUpdateS2CPacket pac && pac.getEntityId() == mc.player.getId()) {
            handleVelocityPacket(e, pac);
        }

        handleAdditionalPackets(e);
    }

    
    private void handleVelocityPacket(PacketEvent e, EntityVelocityUpdateS2CPacket pac) {
        double velX = pac.getVelocityX() / 8000.0;
        double velY = pac.getVelocityY() / 8000.0;
        double velZ = pac.getVelocityZ() / 8000.0;

        switch (mode.getSelected()) {
            case "Matrix":
                if (!flag) {
                    e.setCancelled(true);
                    flag = true;
                } else {
                    flag = false;
                    e.setCancelled(true);
                    pendingVelocity = new Vec3d(
                            velX * -0.1,
                            velY,
                            velZ * -0.1
                    );
                }
                break;
            case "Normal":
                e.setCancelled(true);
                break;
            case "OldGrim":
                e.setCancelled(true);
                grimTicks = 6;
                break;
            case "NewGrim":
                e.setCancelled(true);
                flag = true;
                break;
        }
    }

    
    private void handleAdditionalPackets(PacketEvent e) {
        if (mode.isSelected("OldGrim") && e.getPacket() instanceof CommonPingS2CPacket && grimTicks > 0) {
            e.setCancelled(true);
            grimTicks--;
        }

        if (e.getPacket() instanceof PlayerPositionLookS2CPacket && mode.isSelected("NewGrim")) {
            ccCooldown = 5;
        }
    }

    @EventHandler
    
    public void onTick(TickEvent e) {
        if (!state || mc.player == null || mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) return;

        if (mode.isSelected("Matrix")) {
            handleMatrixTick();
        }

        if (mode.isSelected("NewGrim") && flag) {
            handleNewGrimTick();
        }

        if (grimTicks > 0) {
            grimTicks--;
        }
    }

    
    private void handleMatrixTick() {
        if (pendingVelocity != null) {
            mc.player.setVelocity(pendingVelocity);
            pendingVelocity = null;
        }

        if (mc.player.hurtTime > 0 && !mc.player.isOnGround()) {
            double yaw = mc.player.getYaw() * 0.017453292F;
            double speed = Math.sqrt(mc.player.getVelocity().x * mc.player.getVelocity().x + mc.player.getVelocity().z * mc.player.getVelocity().z);
            mc.player.setVelocity(-Math.sin(yaw) * speed, mc.player.getVelocity().y, Math.cos(yaw) * speed);
            mc.player.setSprinting(mc.player.age % 2 != 0);
        }
    }

    
    private void handleNewGrimTick() {
        if (ccCooldown <= 0) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(
                    mc.player.getX(),
                    mc.player.getY(),
                    mc.player.getZ(),
                    mc.player.getYaw(),
                    mc.player.getPitch(),
                    mc.player.isOnGround(),
                    false
            ));
            mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                    PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK,
                    BlockPos.ofFloored(mc.player.getEyePos()),
                    Direction.DOWN
            ));
        }
        flag = false;
    }

    @Override
    
    public void activate() {
        super.activate();
        grimTicks = 0;
        flag = false;
        ccCooldown = 0;
        pendingVelocity = null;
    }

    @Override
    
    public void deactivate() {
        super.deactivate();
        pendingVelocity = null;
    }
}