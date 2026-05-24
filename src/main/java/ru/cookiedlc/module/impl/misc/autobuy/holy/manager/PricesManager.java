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
import ru.cookiedlc.module.impl.misc.autobuy.holy.model.TickDelayer;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.item.EnumItemType;
import ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.manager.TelegramBotManager;
import ru.cookiedlc.module.impl.misc.autobuy.holy.util.ItemDetector;

import java.util.List;
import java.util.Random;

public class PricesManager implements QuickLogger {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final PricesManager INSTANCE = new PricesManager();

    @Getter
    @Setter
    private static boolean listen = false;

    @Getter
    @Setter
    private static EnumItemType currentItem = null;

    private static int currentIndex = -1;
    private static int sortIterations = 0;
    private static int refreshAttempts = 0;
    private static final int MAX_REFRESH_ATTEMPTS = 2;

    public static void start() {
        currentIndex = -1;
        sortIterations = 0;
        listen = false;
        AutoBuyData.getInstance().setStatus("Получение цен");
        next();
    }

    public static void stop() {
        listen = false;
        currentItem = null;
        currentIndex = -1;
        sortIterations = 0;
        TickDelayer.stopAllTasks("prices");
    }

    public static void next() {
        AutoBuyData data = AutoBuyData.getInstance();
        do {
            currentIndex++;

            if (currentIndex >= EnumItemType.values().length) {
                currentIndex = -1;
                listen = false;
                currentItem = null;
                data.setStatus("N/A");

                if (mc.player != null) {
                    mc.player.closeHandledScreen();
                }
                int totalPrices = EnumItemType.values().length - data.getDisabledItems().size();
                int foundPrices = (int) data.getPriceForOne().entrySet().stream()
                        .filter(entry -> entry.getValue() > 0)
                        .count();

                INSTANCE.logDirect(Formatting.GREEN + "[AutoBuy] " + Formatting.WHITE + "Получение цен завершено!");
                INSTANCE.logDirect(Formatting.GRAY + "Найдено: " + foundPrices + " из " + totalPrices);
                ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.TelegramNotifier
                        .sendPricesUpdateNotification(totalPrices, foundPrices);
                if (foundPrices > 0
                        && TelegramBotManager.isRunning()) {
                    TickDelayer.runTaskLater(() -> {
                        BuyingManager.start();
                        ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.TelegramNotifier.sendMessage(
                                "🛒 *Автобай запущен!*\n\nНачинаю поиск выгодных предметов...");
                    }, 20, "prices");
                }

                return;
            }

            currentItem = EnumItemType.values()[currentIndex];

        } while (data.getDisabledItems().contains(currentItem));

        sortIterations = 0;
        refreshAttempts = 0;

        if (mc.player != null) {
            mc.player.closeHandledScreen();
            mc.player.networkHandler.sendChatMessage("/ah search " + currentItem.getSearchString());
            listen = true;
        }
    }

    public static void onScreenOpen(HandledScreen<?> screen) {
        if (!listen || !(screen instanceof GenericContainerScreen))
            return;
        if (mc.player == null)
            return;

        String title = screen.getTitle().getString();
        if (title.startsWith("Аукцион (")) {
            listen = false;
            TickDelayer.runTaskLater(() -> checkInventory(screen.getScreenHandler()), 3, "prices");
        }
    }

    private static void checkInventory(ScreenHandler handler) {
        if (mc.player == null || mc.interactionManager == null)
            return;

        if (switchSort(handler)) {
            TickDelayer.runTaskLater(() -> checkInventory(handler), 10, "prices");
            return;
        }

        TickDelayer.runTaskLater(() -> lookToSlots(handler), 5, "prices");
    }

    private static void lookToSlots(ScreenHandler handler) {
        AutoBuyData data = AutoBuyData.getInstance();
        if (mc.player == null)
            return;

        for (Slot slot : handler.slots) {
            ItemStack stack = slot.getStack();
            if (stack.isEmpty())
                continue;

            EnumItemType itemType = ItemDetector.detectItem(stack);
            if (itemType == null || itemType != currentItem)
                continue;

            List<Text> loreTexts = getLore(stack);
            for (Text text : loreTexts) {
                String lore = text.getString();
                if (lore.contains("Цена за 1 ед.: ")) {
                    try {
                        String priceStr = lore.replaceAll("[^0-9]", "");
                        if (priceStr.length() > 1) {
                            priceStr = priceStr.substring(1);
                        }
                        int price = Integer.parseInt(priceStr);

                        if (mc.player != null) {
                            INSTANCE.logDirect(Formatting.GREEN + "[AutoBuy] " + Formatting.WHITE
                                    + currentItem.getName() + Formatting.GRAY + " - " + Formatting.YELLOW + price
                                    + Formatting.GRAY + " монет");
                        }

                        data.getPriceForOne().put(currentItem, price);
                        TickDelayer.runTaskLater(PricesManager::next, 20, "prices");
                        return;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        refreshAttempts++;
        if (refreshAttempts >= MAX_REFRESH_ATTEMPTS) {

            data.getPriceForOne().put(currentItem, -1);
            INSTANCE.logDirect(Formatting.RED + "[AutoBuy] " + Formatting.WHITE + currentItem.getName()
                    + Formatting.GRAY + " - не найден");
            TickDelayer.runTaskLater(PricesManager::next, 20, "prices");
            return;
        }

        int delay = new Random().nextInt(61) + 30;
        TickDelayer.runTaskLater(() -> {
            if (mc.player == null || mc.interactionManager == null)
                return;

            int syncId = mc.player.currentScreenHandler.syncId;
            mc.interactionManager.clickSlot(syncId, 47, 0, SlotActionType.PICKUP, mc.player);

            TickDelayer.runTaskLater(() -> {
                if (mc.player != null && mc.player.currentScreenHandler != null) {
                    lookToSlots(mc.player.currentScreenHandler);
                }
            }, 5, "prices");
        }, delay, "prices");
    }

    private static boolean switchSort(ScreenHandler handler) {
        if (mc.player == null || mc.interactionManager == null)
            return false;

        if (sortIterations >= 10) {
            INSTANCE.logDirect(
                    Formatting.RED + "[AutoBuy] Не удалось переключить сортировку для " + currentItem.getName());
            return false;
        }

        Slot sortingSlot = handler.slots.get(52);
        ItemStack stack = sortingSlot.getStack();
        List<Text> loreTexts = getLore(stack);

        for (Text text : loreTexts) {
            String lore = text.getString();
            if (lore.equalsIgnoreCase("✔ Сначала дешевые за ед. товара")) {
                return false;
            }
        }

        int syncId = mc.player.currentScreenHandler.syncId;
        mc.interactionManager.clickSlot(syncId, 52, 0, SlotActionType.PICKUP, mc.player);
        sortIterations++;
        return true;
    }

    private static List<Text> getLore(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore != null) {
            return lore.lines();
        }
        return List.of(stack.getName());
    }
}
