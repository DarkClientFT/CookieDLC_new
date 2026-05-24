package ru.cookiedlc.ui.hud.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import ru.cookiedlc.ui.hud.api.AbstractDraggable;
import ru.cookiedlc.api.system.animation.Animation;
import ru.cookiedlc.api.system.animation.Direction;
import ru.cookiedlc.api.system.animation.implement.DecelerateAnimation;
import ru.cookiedlc.api.system.font.FontRenderer;
import ru.cookiedlc.api.system.font.Fonts;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.common.util.render.Render2DUtil;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.module.impl.render.Hud;

import java.util.*;

import static ru.cookiedlc.api.system.font.Fonts.Type.BOLD;
import static ru.cookiedlc.api.system.font.Fonts.Type.ICO3;

public class StaffList extends AbstractDraggable {

    public final Map<PlayerListEntry, Animation> list = new HashMap<>();
    private final List<String> staffPrefix = List.of(
            "helper", "moder", "staff", "admin", "curator",
            "стажёр", "сотрудник", "помощник", "админ", "модер"
    );

    private static final Map<String, Integer> PREFIX_COLORS = new HashMap<>();
    static {
        PREFIX_COLORS.put("media",    0xFFFF5555);
        PREFIX_COLORS.put("yt",       0xFFFF5555);
        PREFIX_COLORS.put("d.helper", 0xFFFFFF55);
        PREFIX_COLORS.put("helper",   0xFFFFAA00);
        PREFIX_COLORS.put("ml.moder", 0xFF55FFFF);
        PREFIX_COLORS.put("moder",    0xFF5555FF);
        PREFIX_COLORS.put("moder+",   0xFF5555FF);
        PREFIX_COLORS.put("st.moder", 0xFFAA00AA);
        PREFIX_COLORS.put("gl.moder", 0xFFAA00AA);
        PREFIX_COLORS.put("ml.admin", 0xFF55FFFF);
        PREFIX_COLORS.put("admin",    0xFFFF5555);
        PREFIX_COLORS.put("Vanish",   0xFFFF5555);
    }

    private static final float H = 14f;

    public static StaffList getInstance() {
        return Instance.getDraggable(StaffList.class);
    }

    public StaffList() {
        super("Staff List", 115, 40, 100, 23, true);
    }

    @Override
    public boolean visible() {
        return !list.isEmpty() || PlayerIntersectionUtil.isChat(mc.currentScreen);
    }

    @Override
    public void tick() {
        Collection<PlayerListEntry> playerList = Objects.requireNonNull(mc.player).networkHandler.getPlayerList();

        for (PlayerListEntry entry : playerList) {
            GameProfile profile = entry.getProfile();
            Text displayName = entry.getDisplayName();
            if (displayName == null || profile == null) continue;

            String prefix = displayName.getString().replace(profile.getName(), "");
            if (prefix.length() < 2) continue;

            PlayerListEntry player = new PlayerListEntry(profile, false);
            player.setDisplayName(displayName);

            if (list.keySet().stream().noneMatch(p -> Objects.equals(p.getDisplayName(), player.getDisplayName()))) {
                staffPrefix.stream().filter(s -> prefix.toLowerCase().contains(s)).findFirst().ifPresent(s -> {
                    list.put(player, new DecelerateAnimation().setMs(150).setValue(1));
                    if (Hud.getInstance().notificationSettings.isSelected("Staff Join")) {
                        Notifications.getInstance().addList(
                                Text.empty().append(player.getDisplayName()).append(" - Зашел на сервер!"),
                                5000
                        );
                    }
                });
            }
        }

        list.entrySet().stream()
                .filter(s -> playerList.stream().noneMatch(p -> Objects.equals(s.getKey().getDisplayName(), p.getDisplayName())))
                .forEach(s -> s.getValue().setDirection(Direction.BACKWARDS));
        list.values().removeIf(s -> s.isFinished(Direction.BACKWARDS));
    }

    @Override
    public void drawDraggable(DrawContext context) {
        MatrixStack ms = context.getMatrices();

        FontRenderer bold   = Fonts.getSize(13, BOLD);
        FontRenderer boldLg = Fonts.getSize(15, BOLD);
        FontRenderer ico    = Fonts.getSize(14, ICO3);

        int theme    = ColorUtil.getClientColor();
        int themeDim = darken(theme, 0.3f);
        int txtColor = 0xFFDDDAEB;
        int bgColor  = 0xCC111118;
        int bdColor  = 0xFF23232D;

        float posX = getX();
        float posY = getY();
        float gap  = 2f;

        Collection<PlayerListEntry> playerList = Objects.requireNonNull(mc.player).networkHandler.getPlayerList();

        String icoChar   = "W";
        float  icoW      = ico.getStringWidth(icoChar);
        int    staffCount = list.size();
        String titleText = "Staff online";
        float  titleW    = bold.getStringWidth(titleText);

        float headerW = 5f + icoW + 5f + 1f + 5f + titleW + 5f;
        drawRect(ms, posX, posY, headerW, H, bgColor, bdColor);

        float hx = posX + 5f;
        ico.drawString(ms, icoChar, hx, posY + 7f, theme);
        hx += icoW + 5f;
        drawSep(ms, hx, posY, H, 0x40FFFFFF);
        hx += 5f;
        bold.drawString(ms, titleText, hx, posY + 6f, txtColor);

        float currentY = posY + H + gap;
        float maxRowW  = headerW;

        List<Map.Entry<PlayerListEntry, Animation>> entries = new ArrayList<>(list.entrySet());

        if (entries.isEmpty() && PlayerIntersectionUtil.isChat(mc.currentScreen)) {
            maxRowW = Math.max(maxRowW, drawPlayerRow(context, ms, bold, boldLg,
                    posX, currentY, gap, bgColor, bdColor, theme, themeDim, txtColor,
                    "ExampleStaff", "admin", true, 1f));
            currentY += H + gap;
        }

        for (Map.Entry<PlayerListEntry, Animation> entry : entries) {
            PlayerListEntry player = entry.getKey();
            if (player == null) continue;

            float anim = entry.getValue().getOutput().floatValue();
            String name = player.getProfile().getName();

            boolean isOnline = playerList.stream().anyMatch(p -> p.getProfile().getName().equals(name));
            PlayerListEntry renderEntry = isOnline
                    ? playerList.stream().filter(p -> p.getProfile().getName().equals(name)).findFirst().orElse(player)
                    : player;

            String displayName = renderEntry.getDisplayName() != null ? renderEntry.getDisplayName().getString() : name;
            String prefix = getPrefix(displayName);

            float rowW = drawPlayerRow(context, ms, bold, boldLg,
                    posX, currentY, gap, bgColor, bdColor, theme, themeDim, txtColor,
                    name, prefix, isOnline, anim);
            maxRowW = Math.max(maxRowW, rowW);
            currentY += (H + gap) * anim;
        }

        setWidth((int) maxRowW);
        setHeight((int) (currentY - posY));
    }

    private float drawPlayerRow(DrawContext context, MatrixStack ms,
                                FontRenderer bold, FontRenderer boldLg,
                                float posX, float rowY, float gap,
                                int bgColor, int bdColor,
                                int theme, int themeDim, int txtColor,
                                String name, String prefix,
                                boolean isOnline, float animScale) {
        FontRenderer ico = Fonts.getSize(14, ICO3);

        String prefixTag  = "[" + prefix + "]";
        float  prefixW    = bold.getStringWidth(prefixTag);
        float  nameW      = boldLg.getStringWidth(name);
        int    prefixColor = PREFIX_COLORS.getOrDefault(prefix, 0xFFFF5555);

        float dotSize = 4f;
        int   dotColor = isOnline ? 0xFF4BFF65 : 0xFFFF4B4B;

        float rowW = 5f + dotSize + 4f + prefixW + 5f + nameW + 7f;
        float centerY = rowY + H / 2f;

        MathUtil.scale(ms, posX + rowW / 2f, rowY + H / 2f, 1, animScale, () -> {
            drawRect(ms, posX, rowY, rowW, H, bgColor, bdColor);

            float rx = posX + 5f;

            renderShape(ms, rx, centerY - dotSize / 2f, dotSize, dotSize, ShapeProperties.create(ms, rx, centerY - dotSize / 2f, dotSize, dotSize)
                    .round(2)
                    .color(dotColor)
                    .build());
            rx += dotSize + 4f;

            bold.drawString(ms, prefixTag, rx, rowY + 6f, prefixColor);
            rx += prefixW + 5f;

            boldLg.drawGradientString(ms, name, rx, rowY + 5f, theme, themeDim);
        });

        return rowW;
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

    private String getPrefix(String displayName) {
        Map<String, String> charToName = new LinkedHashMap<>();
        charToName.put("ꔀ", "player");    charToName.put("ꔄ", "hero");
        charToName.put("ꔈ", "titan");     charToName.put("ꔒ", "avenger");
        charToName.put("ꔖ", "overlord");  charToName.put("ꔠ", "magister");
        charToName.put("ꔤ", "imperator"); charToName.put("ꔨ", "dragon");
        charToName.put("ꔲ", "bull");      charToName.put("ꕒ", "rabbit");
        charToName.put("ꔶ", "tiger");     charToName.put("ꕄ", "dracula");
        charToName.put("ꕖ", "bunny");     charToName.put("ꕀ", "hydra");
        charToName.put("ꕈ", "cobra");     charToName.put("ꔁ", "media");
        charToName.put("ꔅ", "yt");        charToName.put("ꕠ", "d.helper");
        charToName.put("ꔉ", "helper");    charToName.put("ꔓ", "ml.moder");
        charToName.put("ꔗ", "moder");     charToName.put("ꔡ", "moder+");
        charToName.put("ꔥ", "st.moder"); charToName.put("ꔩ", "gl.moder");
        charToName.put("ꔳ", "ml.admin"); charToName.put("ꔷ", "admin");

        for (Map.Entry<String, String> entry : charToName.entrySet()) {
            if (displayName.contains(entry.getKey())) return entry.getValue();
        }
        return "Vanish";
    }
}