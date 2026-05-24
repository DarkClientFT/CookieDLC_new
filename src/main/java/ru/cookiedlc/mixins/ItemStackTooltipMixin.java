package ru.cookiedlc.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.cookiedlc.common.util.auction.AuctionPriceParser;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    private static final AuctionPriceParser AUCTION_PRICE_PARSER = new AuctionPriceParser();
    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    private void appendAuctionUnitPrice(CallbackInfoReturnable<List<Text>> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!(mc.currentScreen instanceof GenericContainerScreen screen) || !isAuctionScreen(screen)) return;
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.isEmpty() || stack.getCount() <= 1) return;
        int totalPrice = AUCTION_PRICE_PARSER.getPrice(stack);
        if (totalPrice <= 0) return;
        int unitPrice = Math.max(1, Math.round((float) totalPrice / (float) Math.max(1, stack.getCount())));
        List<Text> original = cir.getReturnValue();
        List<Text> tooltip = new ArrayList<>(original);
        int insertAt = findPriceLineIndex(tooltip);
        Text unitLine = Text.literal("§2$ §fЗа 1 шт: §a" + formatPrice(unitPrice));
        if (insertAt >= 0 && insertAt + 1 <= tooltip.size()) {
            tooltip.add(insertAt + 1, unitLine); } else {
            tooltip.add(unitLine);}
        cir.setReturnValue(tooltip);}
    private static boolean isAuctionScreen(GenericContainerScreen screen) {
        String title = screen.getTitle() == null ? "" : screen.getTitle().getString();
        return title.contains("Аукцион") || title.contains("Аукционы") || title.contains("Поиск");}
    private static int findPriceLineIndex(List<Text> tooltip) {
        for (int i = 0; i < tooltip.size(); i++) {
            String line = tooltip.get(i).getString();
            if (line == null || line.isEmpty()) continue;

            String lower = line.toLowerCase();
            if (lower.contains("цена") || lower.contains("price") || line.contains("$")) {
                return i;
            }}
        return -1;}
    private static String formatPrice(int price) {
        String value = String.valueOf(Math.max(0, price));
        StringBuilder builder = new StringBuilder(value.length() + value.length() / 3);
        for (int i = 0; i < value.length(); i++) {
            if (i > 0 && (value.length() - i) % 3 == 0) {
                builder.append('.');}
            builder.append(value.charAt(i));}
        return builder.toString();
    }
}
