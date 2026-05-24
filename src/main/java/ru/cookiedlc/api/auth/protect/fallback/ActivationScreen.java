package ru.cookiedlc.api.auth.protect.fallback;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.cookiedlc.api.auth.AuthManager;
import ru.cookiedlc.api.auth.protect.CookieProtect;
import ru.cookiedlc.common.QuickImports;
@CookieProtect
public class ActivationScreen extends Screen implements QuickImports {

    private TextFieldWidget keyField;
    private ButtonWidget activateButton;
    private String statusMessage = "";
    private Formatting statusColor = Formatting.WHITE;
    private boolean isLoading = false;
    private boolean isBanned = false;
    private String banMessage = "";

    public ActivationScreen() {
        super(Text.literal("Активация"));
    }

    @Override
    protected void init() {
        keyField = new TextFieldWidget(
                textRenderer,
                width / 2 - 100,
                height / 2 - 10,
                200,
                20,
                Text.literal("Ключ активации")
        );
        keyField.setMaxLength(19);
        keyField.setPlaceholder(Text.literal("XXXX-XXXX-XXXX-XXXX").formatted(Formatting.DARK_GRAY));

        String savedKey = AuthManager.getInstance().loadSavedKey();
        if (savedKey != null) {
            keyField.setText(savedKey);
        }

        addDrawableChild(keyField);

        activateButton = ButtonWidget.builder(Text.literal("Активировать"), button -> {
            activate();
        }).dimensions(width / 2 - 100, height / 2 + 20, 200, 20).build();

        addDrawableChild(activateButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("Назад"), button -> {
            mc.setScreen(new TitleScreen());
        }).dimensions(width / 2 - 100, height / 2 + 50, 200, 20).build());
    }

    private void activate() {
        String key = keyField.getText().trim();

        if (key.isEmpty()) {
            statusMessage = "Введите ключ активации!";
            statusColor = Formatting.RED;
            isBanned = false;
            return;
        }

        isLoading = true;
        isBanned = false;
        statusMessage = "Проверка ключа...";
        statusColor = Formatting.YELLOW;
        activateButton.active = false;

        AuthManager.getInstance().authenticate(key).thenAccept(result -> {
            mc.execute(() -> {
                isLoading = false;
                activateButton.active = true;

                if (result.success) {
                    statusMessage = result.message;
                    statusColor = Formatting.GREEN;
                    isBanned = false;
                    mc.setScreen(new TitleScreen());
                } else if (result.banned) {
                    isBanned = true;
                    banMessage = result.message;
                    statusMessage = "";
                } else {
                    statusMessage = result.message;
                    statusColor = Formatting.RED;
                    isBanned = false;
                }
            });
        });
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int panelX = width / 2 - 130;
        int panelY = height / 2 - 90;
        int panelWidth = 260;
        int panelHeight = isBanned ? 220 : 200;

        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xDD101020);

        drawBorder(context, panelX, panelY, panelWidth, panelHeight, isBanned ? 0xFFFF3333 : 0xFF4488FF);

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("CookieDLC").formatted(Formatting.BOLD, Formatting.GOLD),
                width / 2,
                height / 2 - 75,
                0xFFFFAA00
        );

        context.drawCenteredTextWithShadow(
                textRenderer,
                Text.literal("— Активация —").formatted(Formatting.GRAY),
                width / 2,
                height / 2 - 60,
                0xFFAAAAAA
        );

        if (isBanned) {

            int banBoxY = height / 2 + 75;
            context.fill(panelX + 10, banBoxY, panelX + panelWidth - 10, banBoxY + 50, 0xCC330000);

            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal("⛔ ВЫ ЗАБАНЕНЫ ⛔").formatted(Formatting.RED, Formatting.BOLD),
                    width / 2,
                    banBoxY + 5,
                    0xFFFF5555
            );

            String reason = banMessage;
            if (reason.length() > 35) {
                reason = reason.substring(0, 32) + "...";
            }
            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal(reason).formatted(Formatting.WHITE),
                    width / 2,
                    banBoxY + 18,
                    0xFFFFFFFF
            );
            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal("Ошибка? Пишите: ").formatted(Formatting.GRAY)
                            .append(Text.literal("@JavaDeofuscator").formatted(Formatting.AQUA, Formatting.UNDERLINE)),
                    width / 2,
                    banBoxY + 33,
                    0xFFFFFFFF
            );
        } else {
            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal("Получите ключ в Telegram боте:").formatted(Formatting.WHITE),
                    width / 2,
                    height / 2 - 40,
                    0xFFFFFFFF
            );

            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal("@authcookiedlc_bot").formatted(Formatting.AQUA, Formatting.UNDERLINE),
                    width / 2,
                    height / 2 - 28,
                    0xFF55FFFF
            );
            if (!statusMessage.isEmpty()) {
                int statusY = height / 2 + 85;
                int statusWidth = textRenderer.getWidth(statusMessage);

                int bgColor, textColor;

                switch (statusColor) {
                    case GREEN:
                        bgColor = 0xAA004400;
                        textColor = 0xFF55FF55;
                        break;
                    case RED:
                        bgColor = 0xAA440000;
                        textColor = 0xFFFF5555;
                        break;
                    case YELLOW:
                        bgColor = 0xAA444400;
                        textColor = 0xFFFFFF55;
                        break;
                    default:
                        bgColor = 0xAA222222;
                        textColor = 0xFFFFFFFF;
                }

                context.fill(
                        width / 2 - statusWidth / 2 - 8,
                        statusY - 4,
                        width / 2 + statusWidth / 2 + 8,
                        statusY + 12,
                        bgColor
                );

                context.drawCenteredTextWithShadow(
                        textRenderer,
                        Text.literal(statusMessage),
                        width / 2,
                        statusY,
                        textColor
                );
            }
        }

        if (isLoading) {
            long time = System.currentTimeMillis();
            String loadingDots = ".".repeat((int) ((time / 500) % 4));
            context.drawCenteredTextWithShadow(
                    textRenderer,
                    Text.literal("Загрузка" + loadingDots).formatted(Formatting.YELLOW),
                    width / 2,
                    height / 2 + 100,
                    0xFFFFFF55
            );
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + 2, color);
        context.fill(x, y + height - 2, x + width, y + height, color);
        context.fill(x, y, x + 2, y + height, color);
        context.fill(x + width - 2, y, x + width, y + height, color);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        if (isBanned) {
            context.fillGradient(0, 0, width, height, 0xFF1a0000, 0xFF330000);
        } else {
            context.fillGradient(0, 0, width, height, 0xFF0f0f23, 0xFF1a1a3e);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}