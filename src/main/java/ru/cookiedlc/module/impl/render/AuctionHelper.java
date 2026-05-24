package ru.cookiedlc.module.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.slot.Slot;
import net.minecraft.item.ItemStack;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.auction.AuctionPriceParser;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.render.Render2DUtil;
import ru.cookiedlc.common.util.task.scripts.Script;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.chat.ChatReceiveEvent;
import ru.cookiedlc.event.events.container.HandledScreenEvent;
import ru.cookiedlc.event.events.keyboard.KeyEvent;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BindSetting;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.ColorSetting;


import java.util.Comparator;
import java.util.List;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuctionHelper extends Module {

    static final Pattern PRICE_PATTERN = Pattern.compile("Цен[аaAАыЫ]?:?\\s*([\\d,\\s\\.]+)", Pattern.CASE_INSENSITIVE);

    static final int CHEAPEST_COLOR = 0xFF4BFF4B;
    static final int BEST_VALUE_COLOR = 0xFF33AAFF;

    final BooleanSetting onlyMending = new BooleanSetting("Only Mending Items", "Only highlight items with Mending enchantment")
            .setValue(false);

    @NonFinal
    Slot cheapestSlot;
    @NonFinal
    Slot bestValueSlot;

    int lastUpdateTick = 0;
    int lastSlotCount = 0;

    public AuctionHelper() {
        super("AuctionHelper", "Auction Helper", ModuleCategory.RENDER);
        setup(onlyMending);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) {
            cheapestSlot = null;
            bestValueSlot = null;
            return;
        }

        int currentSlotCount = screen.getScreenHandler().slots.size();

        if (currentSlotCount != lastSlotCount || mc.player.age - lastUpdateTick > 5) {
            lastUpdateTick = mc.player.age;
            lastSlotCount = currentSlotCount;
            updateBestSlots(screen);
        }
    }

    @EventHandler
    public void onHandledScreen(HandledScreenEvent e) {
        if (!(mc.currentScreen instanceof GenericContainerScreen screen)) return;

        DrawContext context = e.getDrawContext();
        MatrixStack matrix = context.getMatrices();

        int offsetX = (screen.width - e.getBackgroundWidth()) / 2;
        int offsetY = (screen.height - e.getBackgroundHeight()) / 2;

        matrix.push();
        matrix.translate(offsetX, offsetY, 0);

        long time = System.currentTimeMillis();

        if (cheapestSlot != null && isValidSlot(cheapestSlot, screen)) {
            int color = getBlinkingColor(CHEAPEST_COLOR, time, 500);
            highlightSlot(context, cheapestSlot, color);
        }

        if (bestValueSlot != null && isValidSlot(bestValueSlot, screen) && bestValueSlot != cheapestSlot) {
            int color = getBlinkingColor(BEST_VALUE_COLOR, time, 600);
            highlightSlot(context, bestValueSlot, color);
        }

        matrix.pop();
    }

    private boolean isValidSlot(Slot slot, GenericContainerScreen screen) {
        if (slot == null) return false;
        if (slot.id < 0 || slot.id >= screen.getScreenHandler().slots.size()) return false;
        Slot currentSlot = screen.getScreenHandler().getSlot(slot.id);
        return currentSlot.hasStack() && !currentSlot.getStack().isEmpty();
    }

    private void updateBestSlots(GenericContainerScreen screen) {
        List<Slot> slots = screen.getScreenHandler().slots;
        List<ItemPriceData> validItems = new ArrayList<>();

        for (Slot slot : slots) {
            if (slot.inventory == mc.player.getInventory()) continue;
            ItemStack stack = slot.getStack();
            if (stack.isEmpty()) continue;

            if (onlyMending.isValue()) {
                if (!hasMending(stack)) continue;
            }

            int totalPrice = parsePriceFromLore(stack);
            if (totalPrice <= 0) continue;

            int count = stack.getCount();
            int pricePerItem = totalPrice / count;

            validItems.add(new ItemPriceData(slot, totalPrice, pricePerItem, count));
        }

        if (validItems.isEmpty()) {
            cheapestSlot = null;
            bestValueSlot = null;
            return;
        }

        cheapestSlot = validItems.stream()
                .min(Comparator.comparingInt(d -> d.totalPrice))
                .map(d -> d.slot)
                .orElse(null);

        bestValueSlot = validItems.stream()
                .min(Comparator.comparingInt(d -> d.pricePerItem))
                .map(d -> d.slot)
                .orElse(null);
    }

    private boolean hasMending(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;

        var enchantments = stack.getEnchantments();
        for (var entry : enchantments.getEnchantments()) {
            if (entry.getKey().isPresent()) {
                String enchantmentId = entry.getKey().get().getValue().toString();
                if (enchantmentId.equals("minecraft:mending")) {
                    return true;
                }
            }
        }
        return false;
    }

    private int parsePriceFromLore(ItemStack stack) {
        LoreComponent loreComp = stack.get(DataComponentTypes.LORE);
        if (loreComp == null) return 0;

        for (Text text : loreComp.lines()) {
            String line = Formatting.strip(text.getString());
            if (line == null) continue;
            Matcher m = PRICE_PATTERN.matcher(line);
            if (m.find()) {
                try {
                    String priceStr = m.group(1).replaceAll("[,\\s\\.]", "");
                    return Integer.parseInt(priceStr);
                } catch (NumberFormatException ignored) {}
            }
        }
        return 0;
    }

    private int getBlinkingColor(int color, long time, int periodMs) {
        float alpha = (float) (Math.sin((double) time / periodMs * Math.PI) * 0.3f + 0.7f);
        return ColorUtil.multAlpha(color, Math.min(1f, Math.max(0.4f, alpha)));
    }

    private void highlightSlot(DrawContext context, Slot slot, int color) {
        if (slot != null) {
            Render2DUtil.rectangle.render(
                    ShapeProperties.create(context.getMatrices(), slot.x, slot.y, 16, 16)
                            .color(color)
                            .build()
            );
        }
    }

    private String formatPrice(int price) {
        if (price >= 1_000_000) return String.format("%.2fM", price / 1_000_000.0).replace(",", ".");
        if (price >= 1_000) return String.format("%.1fK", price / 1_000.0).replace(",", ".");
        return String.valueOf(price);
    }

    @lombok.Value
    private static class ItemPriceData {
        Slot slot;
        int totalPrice;
        int pricePerItem;
        int count;
    }
}