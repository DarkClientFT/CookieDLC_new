package ru.cookiedlc.ui.hud.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.registry.Registries;
import ru.cookiedlc.api.auth.protect.CookieProtect;
import ru.cookiedlc.ui.hud.api.AbstractDraggable;
import ru.cookiedlc.api.system.animation.Animation;
import ru.cookiedlc.api.system.animation.Direction;
import ru.cookiedlc.api.system.animation.implement.DecelerateAnimation;
import ru.cookiedlc.api.system.font.FontRenderer;
import ru.cookiedlc.api.system.font.Fonts;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.common.util.other.StringUtil;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.render.Render2DUtil;
import ru.cookiedlc.event.events.packet.PacketEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static ru.cookiedlc.api.system.font.Fonts.Type.BOLD;
import static ru.cookiedlc.api.system.font.Fonts.Type.ICO3;

@CookieProtect
public class CoolDowns extends AbstractDraggable {

    private static final float H   = 14f;
    private static final float GAP = 2f;

    public final List<CoolDown> list = new ArrayList<>();

    private long lastItemChange = 0L;
    private int currentItemIndex = 0;
    private static final Item[] EXAMPLE_ITEMS = {
            Items.ENDER_PEARL, Items.CHORUS_FRUIT, Items.SHIELD,
            Items.WIND_CHARGE, Items.GOLDEN_APPLE, Items.TOTEM_OF_UNDYING,
            Items.ENCHANTED_GOLDEN_APPLE, Items.GOAT_HORN, Items.RECOVERY_COMPASS
    };

    public static CoolDowns getInstance() {
        return Instance.getDraggable(CoolDowns.class);
    }

    public CoolDowns() {
        super("Cool Downs", 10, 40, 80, 23, true);
    }

    @Override
    public boolean visible() {
        return !list.isEmpty() || PlayerIntersectionUtil.isChat(mc.currentScreen);
    }

    @Override
    public void tick() {
        list.removeIf(c -> c.anim.isFinished(Direction.BACKWARDS));
        list.stream()
                .filter(c -> !Objects.requireNonNull(mc.player).getItemCooldownManager().isCoolingDown(c.item.getDefaultStack()))
                .forEach(coolDown -> coolDown.anim.setDirection(Direction.BACKWARDS));

        if (list.isEmpty() && PlayerIntersectionUtil.isChat(mc.currentScreen)) {
            long now = System.currentTimeMillis();
            if (now - lastItemChange >= 1000L) {
                currentItemIndex = (currentItemIndex + 1) % EXAMPLE_ITEMS.length;
                lastItemChange = now;
            }
        }
    }

    @Override
    public void packet(PacketEvent e) {
        if (PlayerIntersectionUtil.nullCheck()) return;
        switch (e.getPacket()) {
            case CooldownUpdateS2CPacket c -> {
                Item item = Registries.ITEM.get(c.cooldownGroup());
                list.stream()
                        .filter(cd -> cd.item.equals(item))
                        .forEach(cd -> cd.anim.setDirection(Direction.BACKWARDS));
                if (c.cooldown() != 0) {
                    list.add(new CoolDown(item,
                            new StopWatch().setMs(-c.cooldown() * 50L),
                            new DecelerateAnimation().setMs(150).setValue(1.0F)));
                }
            }
            case PlayerRespawnS2CPacket p -> list.clear();
            default -> {}
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
        for (CoolDown cd : list) {
            String name = cd.item.getDefaultStack().getName().getString();
            String dur  = StringUtil.getDuration(0);
            float rowW  = 4f + 12f + 4f + normal.getStringWidth(name) + 20f + normal.getStringWidth(dur) + 5f;
            maxRowW = Math.max(maxRowW, rowW);
        }

        String icoChar  = "T";
        float  icoW     = ico.getStringWidth(icoChar);
        String titleTxt = "Cool Downs";
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

        float curY   = posY + H + GAP;
        float centerX = posX + totalW / 2f;

        if (list.isEmpty() && PlayerIntersectionUtil.isChat(mc.currentScreen)) {
            Item item = EXAMPLE_ITEMS[currentItemIndex];
            String name = item.getDefaultStack().getName().getString();
            String dur  = "0:05";
            float rowW  = calcRowW(normal, name, dur);
            totalW = Math.max(totalW, rowW);
            drawCoolDownRow(context, ms, normal, posX, curY, totalW,
                    bgColor, bdColor, sepColor, txtColor, dimColor,
                    item, name, dur, 1f, centerX);
            curY += H + GAP;
        }

        for (CoolDown cd : list) {
            float anim = cd.anim.getOutput().floatValue();
            long elapsed = cd.time.elapsedTime();
            int time = (elapsed >= Integer.MIN_VALUE && elapsed <= Integer.MAX_VALUE)
                    ? (int)(-elapsed / 1000L) : elapsed < 0 ? Integer.MAX_VALUE : Integer.MIN_VALUE;

            String name = cd.item.getDefaultStack().getName().getString();
            String dur  = StringUtil.getDuration(time);
            float rowW  = calcRowW(normal, name, dur);
            totalW = Math.max(totalW, rowW);

            drawCoolDownRow(context, ms, normal, posX, curY, totalW,
                    bgColor, bdColor, sepColor, txtColor, dimColor,
                    cd.item, name, dur, anim, centerX);
            curY += (H + GAP) * anim;
        }

        setWidth((int) totalW);
        setHeight((int) (curY - posY - GAP));
    }

    private void drawCoolDownRow(DrawContext context, MatrixStack ms, FontRenderer font,
                                 float posX, float rowY, float rowW,
                                 int bgColor, int bdColor, int sepColor,
                                 int nameColor, int dimColor,
                                 Item item, String name, String duration,
                                 float anim, float centerX) {
        MathUtil.scale(ms, centerX, rowY + H / 2f, 1, anim, () -> {
            drawRect(ms, posX, rowY, rowW, H, bgColor, bdColor);

            float durW = font.getStringWidth(duration);
            float sepX = posX + rowW - durW - 5f - 1f - 5f;
            drawSep(ms, sepX, rowY, H, sepColor);

            float iconY = rowY + (H - 10f) / 2f;
            Render2DUtil.defaultDrawStack(context, item.getDefaultStack(),
                    posX + 2f, iconY, false, false, 0.5f);

            font.drawString(ms, name, posX + 15f, rowY + 6f, nameColor);

            font.drawString(ms, duration, posX + rowW - durW - 5f, rowY + 6f, dimColor);
        });
    }

    private float calcRowW(FontRenderer font, String name, String duration) {
        return 15f + font.getStringWidth(name) + 20f + font.getStringWidth(duration) + 5f;
    }


    private void drawRect(MatrixStack ms, float x, float y, float w, float h, int bg, int bd) {
        renderShape(ms, x, y, w, h, ShapeProperties.create(ms, x, y, w, h)
                .round(3).softness(1).thickness(1).outlineColor(bd).color(bg).build());
    }

    private void drawSep(MatrixStack ms, float x, float y, float h, int color) {
        rectangle.render(ShapeProperties.create(ms, x, y + (h - 8f) / 2f, 0.5f, 8f)
                .color(color).build());
    }

    private int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * (1f - factor));
        int g = (int)(((color >>  8) & 0xFF) * (1f - factor));
        int b = (int)((color         & 0xFF) * (1f - factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public record CoolDown(Item item, StopWatch time, Animation anim) {}
}