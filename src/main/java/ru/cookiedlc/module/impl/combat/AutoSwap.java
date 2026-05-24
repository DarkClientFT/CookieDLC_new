package ru.cookiedlc.module.impl.combat;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import ru.cookiedlc.common.util.entity.PlayerInventoryUtil;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.keyboard.KeyEvent;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BindSetting;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;

import java.util.Comparator;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoSwap extends Module {
    final BindSetting bind = new BindSetting("Item use key", "Uses item when pressed");

    final SelectSetting firstItem = new SelectSetting("First item", "Select first swap item.")
            .value("Totem of Undying", "Player Head", "Golden Apple", "Shield");

    final SelectSetting secondItem = new SelectSetting("Second item", "Select second swap item.")
            .value("Totem of Undying", "Player Head", "Golden Apple", "Shield");

    private boolean pendingSwap = false;
    private Slot pendingSlot = null;

    public AutoSwap() {
        super("AutoSwap", "Auto Swap", ModuleCategory.COMBAT);
        setup(firstItem, secondItem, bind);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (pendingSwap && pendingSlot != null) {
            PlayerInventoryUtil.swapHand(pendingSlot, Hand.OFF_HAND, true, true);
            pendingSwap = false;
            pendingSlot = null;
        }
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (e.isKeyDown(bind.getKey())) {
            Slot first = PlayerInventoryUtil.getSlot(getItemByType(firstItem.getSelected()), Comparator.comparing(s -> s.getStack().hasEnchantments()), s -> s.id != 46 && s.id != 45);
            Slot second = PlayerInventoryUtil.getSlot(getItemByType(secondItem.getSelected()), Comparator.comparing(s -> s.getStack().hasEnchantments()), s -> s.id != 46 && s.id != 45);
            Slot validSlot = first != null && mc.player.getOffHandStack().getItem() != first.getStack().getItem() ? first : second;
            pendingSlot = validSlot;
            pendingSwap = true;
        }
    }

    private Item getItemByType(String itemType) {
        return switch (itemType) {
            case "Totem of Undying" -> Items.TOTEM_OF_UNDYING;
            case "Player Head" -> Items.PLAYER_HEAD;
            case "Golden Apple" -> Items.GOLDEN_APPLE;
            case "Shield" -> Items.SHIELD;
            default -> Items.AIR;
        };
    }
}