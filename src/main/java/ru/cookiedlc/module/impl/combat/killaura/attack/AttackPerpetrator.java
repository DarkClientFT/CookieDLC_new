package ru.cookiedlc.module.impl.combat.killaura.attack;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.event.events.item.UsingItemEvent;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.module.impl.combat.killaura.rotation.Angle;

import java.util.List;

@Getter
public class AttackPerpetrator implements QuickImports {
    AttackHandler attackHandler = new AttackHandler();

    public void tick() {
        attackHandler.tick();
    }

    public void onPacket(PacketEvent e) {
        attackHandler.onPacket(e);
    }

    public void onUsingItem(UsingItemEvent e) {
        attackHandler.onUsingItem(e);
    }

    public void performAttack(AttackPerpetratorConfigurable configurable) {
        attackHandler.handleAttack(configurable);
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class AttackPerpetratorConfigurable {
        LivingEntity target;
        Angle angle;
        float maximumRange;
        boolean onlyCritical, shouldBreakShield, shouldUnPressShield, useDynamicCooldown, eatAndAttack;
        Box box;
        SelectSetting aimMode;

        public AttackPerpetratorConfigurable(LivingEntity target, Angle angle, float maximumRange, List<String> options, SelectSetting aimMode, Box box) {
            this.target = target;
            this.angle = angle;
            this.maximumRange = maximumRange;
            this.onlyCritical = options.contains("Only Critical");
            this.shouldBreakShield = options.contains("Break Shield");
            this.shouldUnPressShield = options.contains("UnPress Shield");
            this.useDynamicCooldown = options.contains("Dynamic Cooldown");
            this.eatAndAttack = options.contains("No Attack When Eat");
            this.box = box;
            this.aimMode = aimMode;
        }
    }
}
