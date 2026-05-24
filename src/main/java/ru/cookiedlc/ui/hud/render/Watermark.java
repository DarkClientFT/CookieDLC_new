package ru.cookiedlc.ui.hud.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import ru.cookiedlc.api.auth.AuthData;
import ru.cookiedlc.api.auth.protect.loader.Loader;
import ru.cookiedlc.api.system.font.FontRenderer;
import ru.cookiedlc.api.system.font.Fonts;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.ui.hud.api.AbstractDraggable;

import static ru.cookiedlc.api.system.font.Fonts.Type.*;

public class Watermark extends AbstractDraggable {

    private static final float H   = 14f;
    private static final float GAP = 2f;

    private final boolean showLogin  = true;
    private final boolean showFps    = true;
    private final boolean showTime   = true;
    private final boolean showCoords = true;
    private final boolean showPing   = true;
    private final boolean showTps    = true;
    private final boolean showBps    = true;

    public Watermark() {
        super("Watermark", 10, 10, 92, 29, true);
    }

    @Override
    public void tick() {

    }

    @Override
    public void drawDraggable(DrawContext e) {
        MatrixStack ms = e.getMatrices();
        FontRenderer bold  = Fonts.getSize(13, BOLD);
        FontRenderer boldLg = Fonts.getSize(15, BOLD);
        FontRenderer ico   = Fonts.getSize(14, ICO3);
        int theme    = ColorUtil.getClientColor();
        int themeDim = darken(theme, 0.3f);
        int txtColor = 0xFFDDDAEB;
        int sepColor = 0x40FFFFFF;
        int bgColor  = 0xCC111118;
        int bdColor  = 0xFF23232D;
        String nameText   = getUsername();
        String fpsText    = mc.getCurrentFps() * 5 + " Fps";
        String timeText   = java.time.LocalTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        String coordsText = getCoords();
        String pingText   = getPing() + " Ping";
        String tpsText    = "20.0 Ticks";
        String bpsText    = String.format("%.1f Bps", getBps());
        float icoW  = ico.getStringWidth("W");
        float icoX  = ico.getStringWidth("X");
        float icoV  = ico.getStringWidth("V");
        float icoF  = ico.getStringWidth("F");
        float icoQ  = ico.getStringWidth("Q");
        float icoG  = ico.getStringWidth("G");
        float icoAt = ico.getStringWidth("@");
        float posX = getX();
        float posY = getY();
        float maxX = posX;
        float curX = posX;
        float logoNameW = boldLg.getStringWidth("Pasta");
        float logoW = logoNameW + 10f;
        drawRect(ms, curX, posY, logoW, H, bgColor, bdColor);
        boldLg.drawGradientString(ms, "Pasta", curX + 5, posY + 4.5f, theme, themeDim);
        maxX = Math.max(maxX, curX + logoW);
        curX += logoW + GAP;
        float comboW = 5f;
        if (showLogin)  comboW += icoW  + 3 + bold.getStringWidth(nameText)  + 5;
        if (showFps)    comboW += icoX  + 3 + bold.getStringWidth(fpsText)   + 5;
        if (showTime)   comboW += icoV  + 3 + bold.getStringWidth(timeText)  + 10;
        if (showLogin || showFps || showTime) {
            drawRect(ms, curX, posY, comboW, H, bgColor, bdColor);
            maxX = Math.max(maxX, curX + comboW);
            float sx = curX + 5;
            if (showLogin) {
                ico.drawString(ms, "W", sx, posY + 6, theme);
                sx += icoW + 3;
                bold.drawString(ms, nameText, sx - 1, posY + 5.5f, txtColor);
                sx += bold.getStringWidth(nameText) + 3;
                if (showFps || showTime) { drawSep(ms, sx, posY, H, sepColor); sx += 5; }
            }
            if (showFps) {
                ico.drawString(ms, "X", sx, posY + 6, theme);
                sx += icoX + 3;
                bold.drawString(ms, fpsText, sx - 0.5f, posY + 5.5f, txtColor);
                sx += bold.getStringWidth(fpsText) + 3;
                if (showTime) { drawSep(ms, sx, posY, H, sepColor); sx += 5; }
            }
            if (showTime) {
                ico.drawString(ms, "V", sx + 0.5f, posY + 6, theme);
                sx += icoV + 3;
                bold.drawString(ms, timeText, sx, posY + 5.5f, txtColor);
            }
        }
        float row2Y = posY + H + 1f;
        curX = posX;
        if (showCoords) {
            float w = icoF + bold.getStringWidth(coordsText) + 16.5f;
            drawRect(ms, curX, row2Y, w, H, bgColor, bdColor);
            ico.drawString(ms, "F", curX + 5, row2Y + 6, theme);
            drawSep(ms, curX + 8.5f + icoF, row2Y, H, sepColor);
            bold.drawString(ms, coordsText, curX + 13 + icoF, row2Y + 5.5f, txtColor);
            maxX = Math.max(maxX, curX + w);
            curX += w + GAP;
        }
        if (showPing) {
            float w = icoQ + bold.getStringWidth(pingText) + 16.5f;
            drawRect(ms, curX, row2Y, w, H, bgColor, bdColor);
            ico.drawString(ms, "Q", curX + 5, row2Y + 6, theme);
            drawSep(ms, curX + 8.5f + icoQ, row2Y, H, sepColor);
            bold.drawString(ms, pingText, curX + 13 + icoQ, row2Y + 5.5f, txtColor);
            maxX = Math.max(maxX, curX + w);
            curX += w + GAP;
        }
        if (showTps) {
            float w = icoG + bold.getStringWidth(tpsText) + 16.5f;
            drawRect(ms, curX, row2Y, w, H, bgColor, bdColor);
            ico.drawString(ms, "G", curX + 5, row2Y + 6, theme);
            drawSep(ms, curX + 8.5f + icoG, row2Y, H, sepColor);
            bold.drawString(ms, tpsText, curX + 13 + icoG, row2Y + 5.5f, txtColor);
            maxX = Math.max(maxX, curX + w);
            curX += w + GAP;
        }
        if (showBps) {
            float w = icoAt + bold.getStringWidth(bpsText) + 16.5f;
            drawRect(ms, curX, row2Y, w, H, bgColor, bdColor);
            ico.drawString(ms, "@", curX + 5, row2Y + 6, theme);
            drawSep(ms, curX + 8 + icoAt, row2Y, H, sepColor);
            bold.drawString(ms, bpsText, curX + 13 + icoAt, row2Y + 5.5f, txtColor);
            maxX = Math.max(maxX, curX + w);
        }
        setWidth((int)(maxX - posX));
        setHeight((int)(H * 2 + 1));
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
        int b = (int)((color & 0xFF)          * (1f - factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private String getCoords() {
        if (mc.player == null) return "null";
        return (int)mc.player.getX() + ", " + (int)mc.player.getY() + ", " + (int)mc.player.getZ();
    }

    private float getBps() {
        if (mc.player == null) return 0f;
        double dx = mc.player.getX() - mc.player.prevX;
        double dz = mc.player.getZ() - mc.player.prevZ;
        return (float)(Math.sqrt(dx * dx + dz * dz) * 20f);
    }

    private String getUsername() {
        try {
            AuthData authData = AuthData.getInstance();
            if (authData != null && authData.isAuthorized()) {
                String name = authData.getUsername();
                if (name != null && !name.isEmpty()) return name;
            }
        } catch (Exception ignored) {}
        try {
            String name = Loader.getUsername();
            if (name != null && !name.isEmpty()) return name;
        } catch (Exception ignored) {}
        return "null";
    }

    private int getPing() {
        try {
            if (mc.getNetworkHandler() != null && mc.player != null) {
                var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                if (entry != null) return entry.getLatency();
            }
        } catch (Exception ignored) {}
        return 0;
    }
}