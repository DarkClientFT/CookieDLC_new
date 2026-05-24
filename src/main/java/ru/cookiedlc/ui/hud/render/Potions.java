package ru.cookiedlc.ui.hud.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import ru.cookiedlc.ui.hud.api.AbstractDraggable;
import ru.cookiedlc.api.system.font.FontRenderer;
import ru.cookiedlc.api.system.font.Fonts;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.color.ColorUtil;

import java.util.ArrayList;
import java.util.List;

import static ru.cookiedlc.api.system.font.Fonts.Type.BOLD;
import static ru.cookiedlc.api.system.font.Fonts.Type.ICO3;

public class Potions extends AbstractDraggable {

    private static final float H   = 14f;
    private static final float GAP = 2f;

    private List<StatusEffectInstance> potions = new ArrayList<>();

    public Potions() {
        super("Potions", 520, 10, 85, 18, true);
    }

    @Override
    public boolean visible() {
        return !potions.isEmpty() || mc.currentScreen instanceof ChatScreen;
    }

    @Override
    public void tick() {
        if (mc.player != null) {
            potions = mc.player.getStatusEffects()
                    .stream()
                    .filter(effect -> effect.getDuration() > 0)
                    .toList();
        } else {
            potions = new ArrayList<>();
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack ms = context.getMatrices();

        FontRenderer bold   = Fonts.getSize(13, BOLD);
        FontRenderer normal = Fonts.getSize(11, BOLD);
        FontRenderer ico    = Fonts.getSize(14, ICO3);

        int theme    = ColorUtil.getClientColor();
        int themeDim = darken(theme, 0.3f);
        int txtColor = 0xFFDDDAEB;
        int dimColor = 0xFF8888A0;
        int bgColor  = 0xCC111118;
        int bdColor  = 0xFF23232D;
        int sepColor = 0x40FFFFFF;

        float posX = getX();
        float posY = getY();

        float maxRowW = 0f;
        for (StatusEffectInstance effect : potions) {
            String name     = buildName(effect);
            String duration = StatusEffectUtil.getDurationText(effect, 1, 20).getString();
            float rowW = 5f + normal.getStringWidth(name) + 20f + normal.getStringWidth(duration) + 5f;
            maxRowW = Math.max(maxRowW, rowW);
        }

        String icoChar = "E";
        float  icoW    = ico.getStringWidth(icoChar);
        String titleTxt = "Active potions";
        float  titleW   = bold.getStringWidth(titleTxt);
        float  headerW  = 5f + icoW + 5f + 1f + 5f + titleW + 5f;
        float  totalW   = Math.max(headerW, maxRowW);

        drawRect(ms, posX, posY, totalW, H, bgColor, bdColor);

        float hx = posX + 5f;
        ico.drawString(ms, icoChar, hx, posY + 7f, theme);
        hx += icoW + 5f;
        drawSep(ms, hx, posY, H, sepColor);
        hx += 5f;
        bold.drawGradientString(ms, titleTxt, hx, posY + 6f, theme, themeDim);

        float curY = posY + H + GAP;

        if (potions.isEmpty() && mc.currentScreen instanceof ChatScreen) {
            drawEffectRow(ms, normal, posX, curY, totalW, bgColor, bdColor, sepColor, txtColor, dimColor, "Speed II", "0:30");
            curY += H + GAP;
        }

        for (StatusEffectInstance effect : potions) {
            String name     = buildName(effect);
            String duration = StatusEffectUtil.getDurationText(effect, 1, 20).getString();
            drawEffectRow(ms, normal, posX, curY, totalW, bgColor, bdColor, sepColor, txtColor, dimColor, name, duration);
            curY += H + GAP;
        }

        setWidth((int) totalW);
        setHeight((int) (curY - posY - GAP));
    }

    private void drawEffectRow(MatrixStack ms, FontRenderer font,
                               float posX, float rowY, float rowW,
                               int bgColor, int bdColor, int sepColor,
                               int nameColor, int dimColor,
                               String name, String duration) {
        drawRect(ms, posX, rowY, rowW, H, bgColor, bdColor);

        float durW = font.getStringWidth(duration);
        float sepX = posX + rowW - durW - 5f - 1f - 5f;
        drawSep(ms, sepX, rowY, H, sepColor);

        font.drawString(ms, name, posX + 5f, rowY + 6f, nameColor);

        font.drawString(ms, duration, posX + rowW - durW - 5f, rowY + 6f, dimColor);
    }


    private String buildName(StatusEffectInstance effect) {
        String name = effect.getEffectType().value().getName().getString();
        int amp = effect.getAmplifier();
        if (amp > 0) name += " " + (amp + 1);
        return name;
    }


    private void drawRect(MatrixStack ms, float x, float y, float w, float h, int bg, int bd) {
        renderShape(ms, x, y, w, h, ShapeProperties.create(ms, x, y, w, h)
                .round(3)
                .softness(1)
                .thickness(1)
                .outlineColor(bd)
                .color(bg)
                .build());
    }

    private void drawSep(MatrixStack ms, float x, float y, float h, int color) {
        rectangle.render(ShapeProperties.create(ms, x, y + (h - 8f) / 2f, 0.5f, 8f)
                .color(color)
                .build());
    }

    private int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * (1f - factor));
        int g = (int)(((color >>  8) & 0xFF) * (1f - factor));
        int b = (int)((color         & 0xFF) * (1f - factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}