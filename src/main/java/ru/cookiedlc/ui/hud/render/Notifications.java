package ru.cookiedlc.ui.hud.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import ru.cookiedlc.ui.hud.api.AbstractDraggable;
import ru.cookiedlc.api.system.animation.Animation;
import ru.cookiedlc.api.system.animation.Direction;
import ru.cookiedlc.api.system.animation.implement.DecelerateAnimation;
import ru.cookiedlc.api.system.font.FontRenderer;
import ru.cookiedlc.api.system.font.Fonts;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.api.system.sound.SoundManager;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.other.Instance;

import java.util.*;

import static ru.cookiedlc.api.system.font.Fonts.Type.BOLD;
import static ru.cookiedlc.api.system.font.Fonts.Type.ICO3;

public class Notifications extends AbstractDraggable {

    private static final float H   = 14f;
    private static final float GAP = 2f;

    public static Notifications getInstance() {
        return Instance.getDraggable(Notifications.class);
    }

    private final List<Notification> list = new ArrayList<>();

    public Notifications() {
        super("Notifications", 820, 10, 100, 18, true);
    }

    @Override
    public boolean visible() {
        return !list.isEmpty() || mc.currentScreen instanceof ChatScreen;
    }

    @Override
    public void tick() {
        list.forEach(notif -> {
            if (System.currentTimeMillis() > notif.removeTime ||
                    (notif.text.getString().contains("Пример Уведомления")
                            && !PlayerIntersectionUtil.isChat(mc.currentScreen))) {
                notif.anim.setDirection(Direction.BACKWARDS);
            }
        });
        list.removeIf(notif -> notif.anim.isFinished(Direction.BACKWARDS));

        if (mc.currentScreen instanceof ChatScreen && list.isEmpty()) {
            addList("Пример Уведомления", 99999999);
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
        int bgColor  = 0xCC111118;
        int bdColor  = 0xFF23232D;
        int sepColor = 0x40FFFFFF;

        int screenH = mc.getWindow().getScaledHeight();
        int screenW = mc.getWindow().getScaledWidth();
        boolean growDown  = getY() + getHeight() / 2 < screenH / 2;
        boolean alignLeft = getX() + getWidth() / 2 < screenW / 2;

        float maxW = 0f;
        for (Notification n : list) {
            float w = 5f + ico.getStringWidth("O") + 5f + 1f + 5f
                    + normal.getStringWidth(n.text.getString()) + 5f;
            maxW = Math.max(maxW, w);
        }
        float totalW = Math.max(120f, maxW);

        List<Notification> displayList = growDown ? list : reverse(list);
        float offset = 0f;

        for (Notification notif : displayList) {
            float anim = notif.anim.getOutput().floatValue();
            String text = notif.text.getString();

            float x = alignLeft ? getX() : getX() + getWidth() - totalW;
            float y = growDown
                    ? getY() + offset
                    : getY() + getHeight() - offset - H;

            float centerX = x + totalW / 2f;

            MathUtil.scale(ms, centerX, y + H / 2f, 1, anim, () -> {
                drawRect(ms, x, y, totalW, H, bgColor, bdColor);

                float hx = x + 5f;
                ico.drawString(ms, "O", hx, y + 7f, theme);
                hx += ico.getStringWidth("O") + 5f;
                drawSep(ms, hx, y, H, sepColor);
                hx += 5f;
                normal.drawString(ms, text, hx, y + 6f, txtColor);
            });

            offset += (H + GAP) * anim;
        }

        setWidth((int) totalW);
        setHeight(Math.max((int) H, (int) offset));
    }


    public void addList(String text, long durationMs) {
        addList(Text.literal(text), durationMs, null);
    }

    public void addList(Text text, long durationMs) {
        addList(text, durationMs, null);
    }

    public void addList(String text, long durationMs, SoundEvent sound) {
        addList(Text.literal(text), durationMs, sound);
    }

    public void addList(Text text, long durationMs, SoundEvent sound) {
        list.add(new Notification(text,
                new DecelerateAnimation().setMs(300).setValue(1),
                System.currentTimeMillis() + durationMs));

        if (list.size() > 12) list.removeFirst();
        list.sort(Comparator.comparingDouble(n -> -n.removeTime));

        if (sound != null) SoundManager.playSound(sound);
    }


    private List<Notification> reverse(List<Notification> original) {
        List<Notification> r = new ArrayList<>(original);
        Collections.reverse(r);
        return r;
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


    public static class Notification {
        public final Text text;
        public final Animation anim;
        public final long removeTime;

        public Notification(Text text, Animation anim, long removeTime) {
            this.text = text;
            this.anim = anim;
            this.removeTime = removeTime;
        }
    }
}