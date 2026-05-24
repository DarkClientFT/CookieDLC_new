package ru.cookiedlc.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.event.events.player.AttackEvent;
import ru.cookiedlc.event.events.player.SprintEvent;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationController;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Criticals extends Module {
    public static Criticals getInstance() {
        return Instance.get(Criticals.class);
    }

    SelectSetting mode = new SelectSetting("Mode", "Select bypass mode")
            .value("GrimOld", "Grim", "Weak", "Legit");

    boolean attack = false;

    public Criticals() {
        super("Criticals", ModuleCategory.COMBAT);
        setup(mode);
    }

    
    @EventHandler
    public void onAttack(AttackEvent e) {
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;
        
        if (mode.isSelected("GrimOld")) {
            if (!mc.player.isOnGround() && mc.player.fallDistance == 0) {
                PlayerIntersectionUtil.grimSuperBypass$$$(
                    -(mc.player.fallDistance = MathUtil.getRandom(1e-5F, 1e-4F)), 
                    RotationController.INSTANCE.getRotation().random(1e-3F)
                );
            }
        } else if (mode.isSelected("Grim")) {
            boolean onSpace = !mc.options.jumpKey.isPressed() && mc.player.isOnGround();
            if (!onSpace) {
                critPacket(-1.0E-6, true);
            }
        } else if (mode.isSelected("Weak")) {
            if (mc.player.isOnGround() && !mc.player.getAbilities().flying) {
                critPacket(2.71875E-7, false);
                critPacket(0.0, false);
            }
        } else if (mode.isSelected("Legit")) {
            if (mc.player.isOnGround() && !mc.player.getAbilities().flying) {
                mc.player.setSprinting(false);
            }
        }
    }

    
    @EventHandler
    public void onSprint(SprintEvent event) {
        if (mode.isSelected("Legit") && shouldStopSprinting()) {
            event.setSprinting(false);
        }
    }

    private boolean shouldStopSprinting() {
        if (mc.player == null) return false;
        return attack;
    }

    private void critPacket(double yDelta, boolean full) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        
        if (!full) {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX(), 
                mc.player.getY() + yDelta, 
                mc.player.getZ(), 
                false, 
                mc.player.horizontalCollision
            ));
        } else {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.Full(
                mc.player.getX(), 
                mc.player.getY() + yDelta, 
                mc.player.getZ(), 
                mc.player.getYaw(), 
                mc.player.getPitch(), 
                false, 
                mc.player.horizontalCollision
            ));
        }
    }
}
