package ru.cookiedlc.module.impl.misc;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.MultiSelectSetting;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.common.util.entity.PlayerInventoryUtil;
import ru.cookiedlc.event.events.player.TickEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContainerStealer extends Module {
    StopWatch stopWatch = new StopWatch();

    SelectSetting modeSetting = new SelectSetting("Stealer Type", "Selects the type of stealer")
            .value("FunTime", "WhiteList", "Default");
    ValueSetting delaySetting = new ValueSetting("Delay", "Delay between click on slot")
            .setValue(100).range(0, 1000).visible(() -> modeSetting.isSelected("WhiteList") || modeSetting.isSelected("Default"));
    MultiSelectSetting itemSettings = new MultiSelectSetting("Items", "Select the items that the stealer will pick up")
            .value("Player Head", "Totem Of Undying", "Elytra", "Netherite Sword", "Netherite Helmet", "Netherite ChestPlate", "Netherite Leggings", "Netherite Boots", "Netherite Ingot", "Netherite Scrap")
            .visible(() -> modeSetting.isSelected("WhiteList"));

    public ContainerStealer() {
        super("ContainerStealer", "Container Stealer", ModuleCategory.MISC);
        setup(modeSetting, delaySetting, itemSettings);
    }



    
    @EventHandler
    public void onTick(TickEvent e) {
        switch (modeSetting.getSelected()) {
            case "FunTime" -> {
                if (mc.currentScreen instanceof GenericContainerScreen sh && sh.getTitle().getString().toLowerCase().contains("мистический") && !mc.player.getItemCooldownManager().isCoolingDown(Items.GUNPOWDER.getDefaultStack())) {
                    sh.getScreenHandler().slots.stream().filter(s -> s.hasStack() && !s.inventory.equals(mc.player.getInventory()) && stopWatch.every(150))
                            .forEach(s -> PlayerInventoryUtil.clickSlot(s, 0, SlotActionType.QUICK_MOVE, true));
                }
            }
            case "WhiteList", "Default" -> {
                if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler sh) sh.slots.forEach(s -> {
                    if (s.hasStack() && !s.inventory.equals(mc.player.getInventory()) && (modeSetting.isSelected("Default") || whiteList(s.getStack().getItem())) && stopWatch.every(delaySetting.getValue())) {
                        PlayerInventoryUtil.clickSlot(s, 0, SlotActionType.QUICK_MOVE, true);
                    }
                });
            }
        }
    }

    private boolean whiteList(Item item) {
        return itemSettings.getSelected().toString().toLowerCase().contains(item.toString().toLowerCase().replace("_", ""));
    }
}
