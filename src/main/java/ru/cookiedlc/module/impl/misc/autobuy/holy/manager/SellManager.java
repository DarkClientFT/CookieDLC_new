package ru.cookiedlc.module.impl.misc.autobuy.holy.manager;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import ru.cookiedlc.common.QuickLogger;
import ru.cookiedlc.module.impl.misc.autobuy.holy.model.TickDelayer;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.item.EnumItemType;
import ru.cookiedlc.module.impl.misc.autobuy.holy.util.ItemDetector;

public class SellManager implements QuickLogger {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final SellManager INSTANCE = new SellManager();

    @Getter
    @Setter
    private static boolean running = false;

    private static int currentSlot = 0;

    public static void start() {
        if (running)
            return;
        if (AutoBuyData.getInstance().getPriceForOne().isEmpty())
            return;

        running = true;
        currentSlot = 0;
        AutoBuyData.getInstance().setStatus("Продажа");
        processNextSlot();
    }

    public static void stop() {
        running = false;
        AutoBuyData.getInstance().setStatus("N/A");
    }

    private static void processNextSlot() {
        if (!running)
            return;
        if (mc.player == null) {
            stop();
            return;
        }

        PlayerInventory inventory = mc.player.getInventory();
        AutoBuyData data = AutoBuyData.getInstance();

        while (currentSlot < inventory.size()) {
            ItemStack stack = inventory.getStack(currentSlot);
            EnumItemType itemType = ItemDetector.detectItem(stack);

            if (!stack.isEmpty() && itemType != null && data.getPriceForOne().containsKey(itemType)) {
                int pricePerItem = data.getPriceForOne().get(itemType);
                if (pricePerItem > 0) {
                    sellItem(currentSlot, itemType, stack.getCount(), pricePerItem);
                    currentSlot++;
                    return;
                }
            }
            currentSlot++;
        }

        stop();
    }

    private static void sellItem(int slot, EnumItemType itemType, int itemCount, int pricePerItem) {
        if (mc.player == null || mc.interactionManager == null) {
            stop();
            return;
        }

        int hotbarSlot = findEmptyHotbarSlot();
        if (hotbarSlot == -1) {
            hotbarSlot = 0;
        }

        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                slot < 9 ? slot + 36 : slot,
                hotbarSlot,
                SlotActionType.SWAP,
                mc.player);

        mc.player.getInventory().selectedSlot = hotbarSlot;

        int totalPrice = pricePerItem * itemCount;
        String command = "/ah sell " + totalPrice;

        TickDelayer.runTaskLater(() -> {
            if (mc.player != null) {
                mc.player.networkHandler.sendChatMessage(command);
                INSTANCE.logDirect(Formatting.GREEN + "[AutoBuy] " + Formatting.WHITE + "Продаю " + itemType.getName()
                        + " за " + totalPrice);
                TickDelayer.runTaskLater(SellManager::processNextSlot, 3);
            }
        }, 2);
    }

    private static int findEmptyHotbarSlot() {
        if (mc.player == null)
            return -1;

        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
