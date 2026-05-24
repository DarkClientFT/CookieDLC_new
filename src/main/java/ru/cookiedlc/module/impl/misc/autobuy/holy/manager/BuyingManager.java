package ru.cookiedlc.module.impl.misc.autobuy.holy.manager;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.cookiedlc.common.QuickLogger;
import ru.cookiedlc.module.impl.misc.autobuy.holy.antinetvision.AntiDetect;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.model.TickDelayer;
import ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.TelegramNotifier;
import ru.cookiedlc.module.impl.misc.autobuy.holy.item.EnumItemType;
import ru.cookiedlc.module.impl.misc.autobuy.holy.util.ItemDetector;

import java.util.List;
import java.util.Random;

import static ru.cookiedlc.module.impl.misc.autobuy.holy.model.TickDelayer.stopAllTasks;

public class BuyingManager implements QuickLogger {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final BuyingManager INSTANCE = new BuyingManager();

    @Getter
    @Setter
    private static boolean listen = false;

    @Getter
    @Setter
    private static boolean awaitingConfirm = false;

    private static int sortAttempts = 0;
    private static boolean sortingCompleted = false;
    private static EnumItemType pendingPurchaseItem = null;
    private static int pendingPurchasePrice = 0;

    public static void start() {
        if (AutoBuyData.getInstance().getPriceForOne().isEmpty())
            return;
        listen = true;
        awaitingConfirm = false;
        sortAttempts = 0;
        sortingCompleted = false;
        pendingPurchaseItem = null;
        pendingPurchasePrice = 0;
        AutoBuyData.getInstance().setStatus("Покупка");
        AntiDetect.init();
        if (mc.player != null) {
            mc.player.networkHandler.sendChatMessage("/ah");
        }
    }

    public static void stop() {
        listen = false;
        awaitingConfirm = false;
        sortAttempts = 0;
        sortingCompleted = false;
        pendingPurchaseItem = null;
        pendingPurchasePrice = 0;
        AutoBuyData.getInstance().setStatus("N/A");
        AntiDetect.stop();
    }

    public static void onScreenOpen(HandledScreen<?> screen) {
        if (!(screen instanceof GenericContainerScreen))
            return;
        if (mc.player == null)
            return;

        String title = screen.getTitle().getString();

        if (awaitingConfirm && title.startsWith("Покупка предмета")) {
            TickDelayer.runTaskLater(() -> handleConfirmScreen(screen.getScreenHandler()), 4);
            return;
        }

        if (listen && title.startsWith("Аукцион (")) {
            TickDelayer.runTaskLater(() -> checkInventory(screen.getScreenHandler()), 5);
        }
    }

    private static void handleConfirmScreen(ScreenHandler handler) {
        if (mc.player == null || mc.interactionManager == null)
            return;

        stopAllTasks("update auction");
        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, 10, 0, SlotActionType.PICKUP, mc.player);

        awaitingConfirm = false;
        AutoBuyData.getInstance().setItemsBought(AutoBuyData.getInstance().getItemsBought() + 1);

        int delay = new Random().nextInt(61) + 30;
        TickDelayer.runTaskLater(() -> {
            if (mc.player == null || mc.interactionManager == null)
                return;

            int syncId2 = mc.player.currentScreenHandler.syncId;
            mc.interactionManager.clickSlot(syncId2, 47, 0, SlotActionType.PICKUP, mc.player);
            listen = true;
            sortingCompleted = false; 

            TickDelayer.runTaskLater(() -> {
                if (mc.player != null && mc.player.currentScreenHandler != null && listen) {
                    checkInventory(mc.player.currentScreenHandler);
                }
            }, 5, "check auction");
        }, delay, "update auction");
    }

    private static void checkInventory(ScreenHandler handler) {
        AutoBuyData data = AutoBuyData.getInstance();
        if (mc.player == null || mc.interactionManager == null)
            return;

        if (AntiDetect.shouldTakeBreak()) {
            listen = false;
            AntiDetect.startBreak();
            return;
        }

        if (!sortingCompleted && switchSort(handler, "✔ Сначала новые")) {
            TickDelayer.runTaskLater(() -> checkInventory(handler), 10, "sort auction");
            return;
        }

        sortAttempts = 0;

        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty())
                continue;

            EnumItemType itemType = ItemDetector.detectItem(stack);
            if (itemType == null || data.getDisabledItems().contains(itemType))
                continue;

            if (isArmorOrElytra(itemType)) {
                if (!checkDurability(stack, 0.7)) {
                    continue;
                }
            }

            String sellerName = "?";
            List<Text> loreTexts = getLore(stack);

            for (Text text : loreTexts) {
                String lore = text.getString();

                if (lore.contains(" ▍ Продавец: ")) {
                    sellerName = lore.split(" ▍ Продавец: ")[1];
                } else if (lore.contains("Цена за 1 ед.: ")) {
                    if (data.getStaffNicknames().contains(sellerName.toLowerCase())) {
                        continue;
                    }

                    try {
                        String priceStr = lore.replaceAll("[^0-9]", "");
                        if (priceStr.length() > 1) {
                            priceStr = priceStr.substring(1);
                        }
                        int price = Integer.parseInt(priceStr);

                        int targetPrice = data.getPriceForOne().getOrDefault(itemType, -1);
                        if (targetPrice > 0 && price <= targetPrice * data.getMultiplier()) {
                            int allPrice = price * stack.getCount();
                            int availableMoney = data.getMoney();

                            if (allPrice <= availableMoney) {
                                INSTANCE.logDirect(Formatting.GREEN + "[AutoBuy] " + Formatting.WHITE + "Покупаю "
                                        + itemType.getName() + " за " + allPrice);
                                INSTANCE.logDirect(Formatting.GRAY + "Цена/рыночная: " + price + "/" + targetPrice
                                        + " (x" + String.format("%.1f", (double) targetPrice / price) + ")");
                                if (mc.player != null) {
                                    TelegramNotifier.sendPurchaseNotification(
                                            mc.player.getName().getString(),
                                            itemType,
                                            price,
                                            allPrice,
                                            targetPrice,
                                            availableMoney);
                                }

                                int syncId = mc.player.currentScreenHandler.syncId;
                                mc.interactionManager.clickSlot(syncId, slot.id, 0, SlotActionType.PICKUP, mc.player);

                                awaitingConfirm = true;
                                listen = false;

                                data.getHistory().add(new AutoBuyData.BuyHistory(itemType, price, allPrice, targetPrice,
                                        availableMoney));

                                int delay = new Random().nextInt(61) + 30;
                                TickDelayer.runTaskLater(() -> {
                                    if (mc.player == null || mc.interactionManager == null)
                                        return;

                                    int syncId2 = mc.player.currentScreenHandler.syncId;
                                    mc.interactionManager.clickSlot(syncId2, 47, 0, SlotActionType.PICKUP, mc.player);
                                    listen = true;
                                    sortingCompleted = false;

                                    TickDelayer.runTaskLater(() -> {
                                        if (mc.player != null && mc.player.currentScreenHandler != null && listen) {
                                            checkInventory(mc.player.currentScreenHandler);
                                        }
                                    }, 5, "check auction");
                                }, delay, "update auction");
                                return;
                            }
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        int delay = new Random().nextInt(data.getRefreshDelay() / 2) + data.getRefreshDelay();
        TickDelayer.runTaskLater(() -> {
            if (mc.player == null || mc.interactionManager == null)
                return;
            if (!listen)
                return;

            int syncId = mc.player.currentScreenHandler.syncId;
            mc.interactionManager.clickSlot(syncId, 47, 0, SlotActionType.PICKUP, mc.player);

            TickDelayer.runTaskLater(() -> {
                if (mc.player != null && mc.player.currentScreenHandler != null && listen) {
                    checkInventory(mc.player.currentScreenHandler);
                }
            }, 5, "check auction");
        }, delay, "update auction");
    }

    private static boolean switchSort(ScreenHandler handler, String expectedSort) {
        if (mc.player == null || mc.interactionManager == null)
            return false;

        if (sortAttempts >= 10) {
            INSTANCE.logDirect(Formatting.RED + "[AutoBuy] Не удалось переключить сортировку на аукционе");
            sortingCompleted = true;
            return false;
        }

        Slot sortingSlot = handler.slots.get(52);
        ItemStack stack = sortingSlot.getStack();
        List<Text> loreTexts = getLore(stack);

        for (Text text : loreTexts) {
            if (text.getString().equalsIgnoreCase(expectedSort)) {
                sortingCompleted = true;
                return false;
            }
        }

        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, 52, 0, SlotActionType.PICKUP, mc.player);
        sortAttempts++;
        return true;
    }

    private static List<Text> getLore(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) {
            return lore.lines();
        }
        return List.of(stack.getName());
    }

    private static boolean isArmorOrElytra(EnumItemType itemType) {
        return switch (itemType) {
            case HELMET_SUN, HELMET_ETERNITY, HELMET_INFINITY,
                 CHESTPLATE_ETERNITY, CHESTPLATE_INFINITY,
                 LEGGINGS_ETERNITY, LEGGINGS_INFINITY,
                 BOOTS_ETERNITY, BOOTS_INFINITY,
                 ELYTRA, UNBREAKING_ELYTRA ->
                    true;
            default -> false;
        };
    }
    private static boolean checkDurability(ItemStack stack, double minDurabilityPercent) {
       
        if (!stack.isDamageable()) {
            return true; 
        }

        if (stack.get(DataComponentTypes.UNBREAKABLE) != null) {
            return true;
        }

        int maxDamage = stack.getMaxDamage();
        int currentDamage = stack.getDamage();

        if (maxDamage <= 0) {
            return true; 
        }

        double currentDurabilityPercent = (double) (maxDamage - currentDamage) / maxDamage;
        return currentDurabilityPercent >= minDurabilityPercent;
    }
}
