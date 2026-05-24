package ru.cookiedlc.module.impl.misc.autobuy.holy.render;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.item.EnumItemType;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Setter
@Getter
public class AutoBuyScreen extends Screen implements QuickImports {
    private int selectedCategory = 0;
    private String searchText = "";
    private boolean isTypingSearch = false;
    private boolean isTypingMultiplier = false;
    private String multiplierText;
    private float scrollOffset = 0f;
    private float targetScrollOffset = 0f;
    private float settingsScrollOffset = 0f;
    private float settingsTargetScrollOffset = 0f;
    private final float[] switchAnimations = new float[EnumItemType.values().length];

    private final AutoBuyData data = AutoBuyData.getInstance();
    private final Runnable onStartBuying;
    private final Runnable onGetPrices;
    private final Runnable onStartSelling;

    public AutoBuyScreen(Runnable onStartBuying, Runnable onGetPrices, Runnable onStartSelling) {
        super(Text.literal("AutoBuy"));
        this.onStartBuying = onStartBuying;
        this.onGetPrices = onGetPrices;
        this.onStartSelling = onStartSelling;
        this.multiplierText = String.format("%.2f", data.getMultiplier());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int panelWidth = width / 3;
        int panelHeight = height / 3;
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;

        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, panelWidth, panelHeight)
                .round(8).color(new Color(0, 0, 0, 180).getRGB()).build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, panelWidth, panelHeight)
                .round(8).thickness(0.5F).color(new Color(80, 80, 255, 150).getRGB()).build());

        int buttonWidth = panelWidth / 3;
        int buttonHeight = 20;
        int topButtonY = y + 5;

        renderCategoryButton(context, x + 5, topButtonY, buttonWidth - 10, buttonHeight, "Предметы", 0, mouseX, mouseY);
        renderCategoryButton(context, x + buttonWidth + 5, topButtonY, buttonWidth - 10, buttonHeight, "История", 1, mouseX, mouseY);
        renderCategoryButton(context, x + 2 * buttonWidth + 5, topButtonY, buttonWidth - 10, buttonHeight, "Настройки", 2, mouseX, mouseY);

        int contentY = y + buttonHeight + 15;
        int contentHeight = panelHeight - buttonHeight - 20;

        context.enableScissor(x + 10, contentY, x + panelWidth - 10, y + panelHeight - 5);

        if (selectedCategory == 0) {
            renderItemsTab(context, x + 10, contentY, panelWidth - 20, contentHeight, mouseX, mouseY);
        } else if (selectedCategory == 1) {
            renderHistoryTab(context, x + 10, contentY, panelWidth - 20, contentHeight, mouseX, mouseY);
        } else {
            renderSettingsTab(context, x + 10, contentY, panelWidth - 20, contentHeight, mouseX, mouseY);
        }

        context.disableScissor();
        super.render(context, mouseX, mouseY, delta);
    }

    private void renderCategoryButton(DrawContext context, int x, int y, int w, int h, String text, int category, int mouseX, int mouseY) {
        boolean selected = selectedCategory == category;
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int color = selected ? new Color(100, 100, 255, 180).getRGB() :
                   (hovered ? new Color(60, 60, 60, 180).getRGB() : new Color(40, 40, 40, 180).getRGB());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, w, h).round(5).color(color).build());
        context.drawText(textRenderer, text, x + 5, y + 6, 0xFFFFFF, false);
    }

    private void renderItemsTab(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, w, 20)
                .round(5).color(new Color(40, 40, 40, 180).getRGB()).build());
        String searchDisplay = "Поиск: " + searchText + (isTypingSearch && System.currentTimeMillis() % 1000 < 500 ? "_" : "");
        context.drawText(textRenderer, searchDisplay, x + 5, y + 6, 0xFFFFFF, false);

        HashSet<EnumItemType> disabledItems = data.getDisabledItems();
        List<EnumItemType> filteredItems = new ArrayList<>();
        for (EnumItemType item : EnumItemType.values()) {
            if (searchText.isEmpty() || item.getName().toLowerCase().startsWith(searchText.toLowerCase())) {
                filteredItems.add(item);
            }
        }

        if (filteredItems.isEmpty()) {
            context.drawText(textRenderer, "Ничего не найдено", x + 10, y + 30, 0xAAAAAA, false);
            return;
        }

        int itemHeight = 25;
        int contentY = y + 25;
        int visibleHeight = h - 25;
        updateScroll(filteredItems.size() * itemHeight, visibleHeight);

        int firstVisible = (int) (scrollOffset / itemHeight);
        int visibleCount = Math.min(filteredItems.size() - firstVisible, visibleHeight / itemHeight + 2);

        for (int i = 0; i < visibleCount; i++) {
            int idx = firstVisible + i;
            if (idx >= filteredItems.size()) break;
            EnumItemType item = filteredItems.get(idx);
            int itemY = contentY + i * itemHeight - (int) (scrollOffset % itemHeight);
            if (itemY >= contentY - itemHeight && itemY <= contentY + visibleHeight) {
                renderItemEntry(context, x, itemY, w, itemHeight, item, disabledItems.contains(item), mouseX, mouseY);
            }
        }
    }

    private void renderItemEntry(DrawContext context, int x, int y, int w, int h, EnumItemType item, boolean disabled, int mouseX, int mouseY) {
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y, w, h - 2)
                .round(3).color(new Color(30, 30, 30, 150).getRGB()).build());
        context.drawItem(new ItemStack(item.getItemType()), x + 2, y + 3);
        context.drawText(textRenderer, item.getName(), x + 25, y + 8, disabled ? 0xFF5555 : 0xFFFFFF, false);

        if (data.getPriceForOne().containsKey(item)) {
            int price = data.getPriceForOne().get(item);
            String priceText = price > 0 ? String.valueOf(price) : "N/A";
            int priceWidth = textRenderer.getWidth(priceText);
            int priceX = x + w - 60 - priceWidth;
            rectangle.render(ShapeProperties.create(context.getMatrices(), priceX - 5, y + 3, priceWidth + 10, h - 8)
                    .round(3).color(new Color(40, 40, 40, 180).getRGB()).build());
            context.drawText(textRenderer, priceText, priceX, y + 8, price > 0 ? 0xFFFF55 : 0xFF5555, false);
        }

        int switchX = x + w - 45;
        int switchY = y + (h - 20) / 2;
        int switchWidth = 40;
        int switchHeight = 18;

        float animProgress = switchAnimations[item.ordinal()];
        float target = disabled ? 0f : 1f;
        if (Math.abs(animProgress - target) > 0.05f) {
            switchAnimations[item.ordinal()] += (target - animProgress) * 0.3f;
        } else {
            switchAnimations[item.ordinal()] = target;
        }

        int switchColor = disabled ? new Color(255, 85, 85, 200).getRGB() : new Color(85, 255, 85, 200).getRGB();
        rectangle.render(ShapeProperties.create(context.getMatrices(), switchX, switchY, switchWidth, switchHeight)
                .round(9).color(switchColor).build());
        int thumbX = (int) (switchX + switchAnimations[item.ordinal()] * (switchWidth - switchHeight));
        rectangle.render(ShapeProperties.create(context.getMatrices(), thumbX, switchY, switchHeight, switchHeight)
                .round(9).color(0xFFFFFFFF).build());
    }

    private void renderHistoryTab(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {
        List<AutoBuyData.BuyHistory> history = data.getHistory();
        if (history.isEmpty()) {
            context.drawText(textRenderer, "История покупок пуста", x + 10, y + 10, 0xAAAAAA, false);
            return;
        }
        int entryHeight = 40;
        int visibleCount = h / entryHeight;
        for (int i = 0; i < Math.min(history.size(), visibleCount); i++) {
            AutoBuyData.BuyHistory entry = history.get(history.size() - 1 - i);
            int entryY = y + i * entryHeight;
            rectangle.render(ShapeProperties.create(context.getMatrices(), x, entryY, w, entryHeight - 2)
                    .round(3).color(new Color(30, 30, 30, 150).getRGB()).build());
            context.drawText(textRenderer, "Куплено " + entry.itemType().getName() + " за " + entry.allPrice(), x + 5, entryY + 5, 0xFFFFFF, false);
            context.drawText(textRenderer, "Цена/рыночная: " + entry.price() + "/" + entry.auctionPrice(), x + 5, entryY + 15, 0xAAAAAA, false);
            context.drawText(textRenderer, "Баланс до: " + entry.balance(), x + 5, entryY + 25, 0xAAAAAA, false);
        }
    }

    private void renderSettingsTab(DrawContext context, int x, int y, int w, int h, int mouseX, int mouseY) {

        int totalContentHeight = 170;
        float maxScroll = Math.max(0, totalContentHeight - h);
        settingsTargetScrollOffset = Math.min(settingsTargetScrollOffset, maxScroll);
        if (Math.abs(settingsScrollOffset - settingsTargetScrollOffset) > 0.1f) {
            settingsScrollOffset += (settingsTargetScrollOffset - settingsScrollOffset) * 0.3f;
        } else {
            settingsScrollOffset = settingsTargetScrollOffset;
        }

        int scrollY = (int) settingsScrollOffset;

        context.drawText(textRenderer, "Коэффициент покупки (0-1):", x, y - scrollY, 0xFFFFFF, false);
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, y + 15 - scrollY, w, 20)
                .round(5).color(new Color(40, 40, 40, 180).getRGB()).build());
        String multiplierDisplay = multiplierText + (isTypingMultiplier && System.currentTimeMillis() % 1000 < 500 ? "_" : "");
        context.drawText(textRenderer, multiplierDisplay, x + 5, y + 21 - scrollY, 0xFFFFFF, false);

        context.drawText(textRenderer, "Статус: " + data.getStatus(), x, y + 45 - scrollY, 0xFFFFFF, false);
        context.drawText(textRenderer, "Куплено предметов: " + data.getItemsBought(), x, y + 60 - scrollY, 0xFFFFFF, false);
        context.drawText(textRenderer, "Баланс: " + data.getMoney(), x, y + 75 - scrollY, 0xFFFFFF, false);

        int buttonY = y + 95 - scrollY;
        boolean pricesAvailable = !data.getPriceForOne().isEmpty();

        boolean buyHovered = mouseX >= x && mouseX <= x + w && mouseY >= buttonY && mouseY <= buttonY + 20;
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, buttonY, w, 20).round(5)
                .color(pricesAvailable ? (buyHovered ? new Color(85, 255, 85, 220).getRGB() : new Color(85, 255, 85, 180).getRGB())
                        : new Color(100, 100, 100, 180).getRGB()).build());
        context.drawText(textRenderer, pricesAvailable ? "Включить автобай" : "Сначала получите цены", x + 10, buttonY + 6, 0xFFFFFF, false);

        boolean pricesHovered = mouseX >= x && mouseX <= x + w && mouseY >= buttonY + 25 && mouseY <= buttonY + 45;
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, buttonY + 25, w, 20).round(5)
                .color(pricesHovered ? new Color(85, 85, 255, 220).getRGB() : new Color(85, 85, 255, 180).getRGB()).build());
        context.drawText(textRenderer, "Получить цены", x + 10, buttonY + 31, 0xFFFFFF, false);

        boolean sellHovered = mouseX >= x && mouseX <= x + w && mouseY >= buttonY + 50 && mouseY <= buttonY + 70;
        rectangle.render(ShapeProperties.create(context.getMatrices(), x, buttonY + 50, w, 20).round(5)
                .color(pricesAvailable ? (sellHovered ? new Color(255, 200, 85, 220).getRGB() : new Color(255, 200, 85, 180).getRGB())
                        : new Color(100, 100, 100, 180).getRGB()).build());
        context.drawText(textRenderer, pricesAvailable ? "Продать предметы" : "Сначала получите цены", x + 10, buttonY + 56, 0xFFFFFF, false);
    }

    private void updateScroll(int contentHeight, int visibleHeight) {
        float maxScroll = Math.max(0, contentHeight - visibleHeight);
        targetScrollOffset = Math.min(targetScrollOffset, maxScroll);
        if (Math.abs(scrollOffset - targetScrollOffset) > 0.1f) {
            scrollOffset += (targetScrollOffset - scrollOffset) * 0.3f;
        } else {
            scrollOffset = targetScrollOffset;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelWidth = width / 3;
        int panelHeight = height / 3;
        int x = (width - panelWidth) / 2;
        int y = (height - panelHeight) / 2;

        if (mouseX < x || mouseX > x + panelWidth || mouseY < y || mouseY > y + panelHeight) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int buttonWidth = panelWidth / 3;
        int buttonHeight = 20;
        int topButtonY = y + 5;

        if (mouseY >= topButtonY && mouseY <= topButtonY + buttonHeight) {
            if (mouseX >= x + 5 && mouseX <= x + buttonWidth - 5) {
                selectedCategory = 0; scrollOffset = 0; targetScrollOffset = 0; return true;
            }
            if (mouseX >= x + buttonWidth + 5 && mouseX <= x + 2 * buttonWidth - 5) {
                selectedCategory = 1; scrollOffset = 0; targetScrollOffset = 0; return true;
            }
            if (mouseX >= x + 2 * buttonWidth + 5 && mouseX <= x + 3 * buttonWidth - 5) {
                selectedCategory = 2; settingsScrollOffset = 0; settingsTargetScrollOffset = 0; return true;
            }
        }

        int contentY = y + buttonHeight + 15;
        int contentHeight = panelHeight - buttonHeight - 20;
        int contentX = x + 10;
        int contentW = panelWidth - 20;

        if (selectedCategory == 0) {
            if (mouseX >= contentX && mouseX <= contentX + contentW && mouseY >= contentY && mouseY <= contentY + 20) {
                isTypingSearch = true; isTypingMultiplier = false; return true;
            }
            handleItemsClick(contentX, contentY + 25, contentW, contentHeight - 25, mouseX, mouseY);
        } else if (selectedCategory == 2) {
            int scrollY = (int) settingsScrollOffset;

            if (mouseX >= contentX && mouseX <= contentX + contentW &&
                mouseY >= contentY + 15 - scrollY && mouseY <= contentY + 35 - scrollY) {
                isTypingMultiplier = true; isTypingSearch = false; return true;
            }

            int btnY = contentY + 95 - scrollY;
            boolean pricesAvailable = !data.getPriceForOne().isEmpty();

            if (mouseX >= contentX && mouseX <= contentX + contentW) {

                if (mouseY >= btnY && mouseY <= btnY + 20) {
                    if (pricesAvailable) {
                        close();
                        onStartBuying.run();
                    }
                    return true;
                }

                if (mouseY >= btnY + 25 && mouseY <= btnY + 45) {
                    close();
                    onGetPrices.run();
                    return true;
                }

                if (mouseY >= btnY + 50 && mouseY <= btnY + 70) {
                    if (pricesAvailable) {
                        close();
                        onStartSelling.run();
                    }
                    return true;
                }
            }
        }

        isTypingSearch = false;
        isTypingMultiplier = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleItemsClick(int x, int y, int w, int h, double mouseX, double mouseY) {
        if (mouseY < y || mouseY > y + h) return;
        HashSet<EnumItemType> disabledItems = data.getDisabledItems();
        List<EnumItemType> filteredItems = new ArrayList<>();
        for (EnumItemType item : EnumItemType.values()) {
            if (searchText.isEmpty() || item.getName().toLowerCase().startsWith(searchText.toLowerCase())) {
                filteredItems.add(item);
            }
        }
        int itemHeight = 25;
        int firstVisible = (int) (scrollOffset / itemHeight);
        int visibleCount = h / itemHeight + 2;
        for (int i = 0; i < visibleCount; i++) {
            int idx = firstVisible + i;
            if (idx >= filteredItems.size()) break;
            int itemY = y + i * itemHeight - (int) (scrollOffset % itemHeight);
            if (mouseY >= itemY && mouseY <= itemY + itemHeight) {
                EnumItemType item = filteredItems.get(idx);
                int switchX = x + w - 45;
                if (mouseX >= switchX && mouseX <= switchX + 40) {
                    if (disabledItems.contains(item)) disabledItems.remove(item);
                    else disabledItems.add(item);
                }
                return;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (selectedCategory == 2) {
            settingsTargetScrollOffset = Math.max(0, settingsTargetScrollOffset - (float) verticalAmount * 20);
        } else {
            targetScrollOffset = Math.max(0, targetScrollOffset - (float) verticalAmount * 20);
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputUtil.GLFW_KEY_ESCAPE) {
            if (isTypingSearch || isTypingMultiplier) {
                isTypingSearch = false; isTypingMultiplier = false; return true;
            }
            close(); return true;
        }
        if (isTypingSearch) {
            if (keyCode == InputUtil.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1); targetScrollOffset = 0; return true;
            }
            if (keyCode == InputUtil.GLFW_KEY_ENTER) { isTypingSearch = false; return true; }
        }
        if (isTypingMultiplier) {
            if (keyCode == InputUtil.GLFW_KEY_BACKSPACE && !multiplierText.isEmpty()) {
                multiplierText = multiplierText.substring(0, multiplierText.length() - 1); return true;
            }
            if (keyCode == InputUtil.GLFW_KEY_ENTER) {
                isTypingMultiplier = false;
                try {
                    double value = Double.parseDouble(multiplierText.isEmpty() ? "0" : multiplierText);
                    value = Math.max(0, Math.min(1, value));
                    data.setMultiplier(value);
                    multiplierText = String.format("%.2f", value);
                } catch (NumberFormatException e) {
                    multiplierText = String.format("%.2f", data.getMultiplier());
                }
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (isTypingSearch) { searchText += chr; targetScrollOffset = 0; return true; }
        if (isTypingMultiplier && (Character.isDigit(chr) || chr == '.')) { multiplierText += chr; return true; }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() { return false; }
}
