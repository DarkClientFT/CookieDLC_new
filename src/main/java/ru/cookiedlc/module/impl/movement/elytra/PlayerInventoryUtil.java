package ru.cookiedlc.module.impl.movement.elytra;

import net.minecraft.item.Item;

import static ru.cookiedlc.common.QuickImports.mc;

public class PlayerInventoryUtil {

    public static int searchHotbarItem(Item item) {
        for (int i=0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return i;
            }
        }
        return -1;
    }

    public static boolean boolHotbarItem(Item item) {
        for (int i=0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) {
                return true;
            }
        }
        return false;
    }
}
