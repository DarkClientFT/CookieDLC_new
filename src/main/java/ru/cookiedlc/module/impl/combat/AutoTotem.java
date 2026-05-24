package ru.cookiedlc.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;

import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.MultiSelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.common.util.entity.PlayerInventoryUtil;
import ru.cookiedlc.common.util.task.scripts.Script;
import ru.cookiedlc.event.events.player.TickEvent;

import java.util.Comparator;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AutoTotem extends Module {
    ValueSetting healthSetting = new ValueSetting("Max Health", "Maximum health for triggering Auto Totem")
            .setValue(4F).range(2F, 8F);
    MultiSelectSetting triggerSetting = new MultiSelectSetting("Triggers", "Select in which case the Totem will be taken")
            .value("Crystal", "TNT");
    ValueSetting TNTRangeSetting = new ValueSetting("TNT Distance", "Distance to TNT for triggering Auto Totem")
            .setValue(8F).range(0F, 150F).visible(() -> triggerSetting.isSelected("TNT"));

    StopWatch stopWatch = new StopWatch();
    Script script = new Script();

    public AutoTotem() {
        super("AutoTotem", "Auto Totem", ModuleCategory.COMBAT);
        setup(healthSetting, triggerSetting, TNTRangeSetting);
    }

    
    @Override
    public void deactivate() {
        script.update();
        super.deactivate();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (trigger()) {
            ItemStack offHandStack = mc.player.getOffHandStack();
            Slot slot = PlayerInventoryUtil.getSlot(Items.TOTEM_OF_UNDYING, Comparator.comparing(s -> !s.getStack().hasEnchantments()), s -> s.id != 46 && s.id != 45);
            if (slot == null) return;
            boolean needSwap = !offHandStack.getItem().equals(Items.TOTEM_OF_UNDYING) || (offHandStack.hasEnchantments() && !slot.getStack().hasEnchantments());
            if (needSwap && stopWatch.every(200)) {
                PlayerInventoryUtil.swapHand(slot, Hand.OFF_HAND, true, true);
                if (script.isFinished()) {
                    script.cleanup().addTickStep(0, () -> PlayerInventoryUtil.swapHand(slot, Hand.OFF_HAND, true, true));
                }
            }
        } else if (!script.isFinished() && stopWatch.every(200)) {
            script.update();
        }
    }

    public boolean trigger() {
        float elytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem().equals(Items.ELYTRA) ? 4 : 0;
        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (mc.player.getItemCooldownManager().isCoolingDown(Items.TOTEM_OF_UNDYING.getDefaultStack())) return false;
        if (health < healthSetting.getValue() + elytra) return true;
        if (triggerSetting.isSelected("Crystal") && PlayerIntersectionUtil.streamEntities().anyMatch(e -> e instanceof EndCrystalEntity && mc.player.distanceTo(e) < 5 && e.getY() > mc.player.getEyeY()))
            return true;
        return triggerSetting.isSelected("TNT") && PlayerIntersectionUtil.streamEntities().anyMatch(e -> e instanceof TntEntity && mc.player.distanceTo(e) < TNTRangeSetting.getValue());
    }
}
