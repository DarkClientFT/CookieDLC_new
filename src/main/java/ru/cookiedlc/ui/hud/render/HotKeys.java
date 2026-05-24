package ru.cookiedlc.ui.hud.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.glfw.GLFW;
import ru.cookiedlc.ui.hud.api.AbstractDraggable;
import ru.cookiedlc.api.system.font.FontRenderer;
import ru.cookiedlc.api.system.font.Fonts;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.module.api.Module;

import java.util.ArrayList;
import java.util.List;

import static ru.cookiedlc.api.system.font.Fonts.Type.BOLD;
import static ru.cookiedlc.api.system.font.Fonts.Type.ICO3;

public class HotKeys extends AbstractDraggable {

    private static final float H   = 14f;
    private static final float GAP = 2f;

    private List<Module> modules = new ArrayList<>();

    public HotKeys() {
        super("Hot Keys", 420, 10, 75, 18, true);
    }

    @Override
    public boolean visible() {
        return !modules.isEmpty() || mc.currentScreen instanceof ChatScreen;
    }

    @Override
    public void tick() {
        modules = Main.getInstance().getModuleProvider().getModules().stream()
                .filter(module -> module.getAnimation().getOutput().floatValue() != 0 && module.getKey() != -1)
                .toList();
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
        for (Module module : modules) {
            float nameW = normal.getStringWidth(module.getName());
            float keyW  = normal.getStringWidth(getKeyName(module.getKey()));
            float rowW = 5f + nameW + 25f + keyW + 5f;
            maxRowW = Math.max(maxRowW, rowW);
        }

        String icoChar  = "C";
        float  icoW     = ico.getStringWidth(icoChar);
        String titleTxt = "Hotkeys";
        float  titleW   = bold.getStringWidth(titleTxt);
        float  headerW  = 5f + icoW + 5f + 1f + 5f + titleW + 5f;

        float totalW = Math.max(headerW, maxRowW);

        drawRect(ms, posX, posY, totalW, H, bgColor, bdColor);

        float hx = posX + 5f;
        ico.drawString(ms, icoChar, hx, posY + 7f, theme);
        hx += icoW + 5f;
        drawSep(ms, hx, posY, H, sepColor);
        hx += 5f;
        bold.drawGradientString(ms, titleTxt, hx, posY + 6f, theme, themeDim);

        float curY = posY + H + GAP;

        if (modules.isEmpty() && mc.currentScreen instanceof ChatScreen) {
            drawModuleRow(ms, normal, posX, curY, totalW, bgColor, bdColor, sepColor,
                    txtColor, dimColor, "Example", "[X]");
            curY += H + GAP;
        }

        for (Module module : modules) {
            String keyName = getKeyName(module.getKey());
            drawModuleRow(ms, normal, posX, curY, totalW, bgColor, bdColor, sepColor,
                    txtColor, dimColor, module.getName(), keyName);
            curY += H + GAP;
        }

        setWidth((int) totalW);
        setHeight((int) (curY - posY - GAP));
    }

    private void drawModuleRow(MatrixStack ms, FontRenderer font,
                               float posX, float rowY, float rowW,
                               int bgColor, int bdColor, int sepColor,
                               int nameColor, int keyColor,
                               String name, String key) {
        drawRect(ms, posX, rowY, rowW, H, bgColor, bdColor);

        float keyW = font.getStringWidth(key);
        float sepX = rowW - keyW - 5f - 1f - 6f;

        font.drawString(ms, name, posX + 5f, rowY + 6f, nameColor);

        drawSep(ms, posX + sepX, rowY, H, sepColor);

        font.drawString(ms, key, posX + rowW - keyW - 5f, rowY + 6f, keyColor);
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

    private String getKeyName(int keyCode) {
        if (keyCode == -1) return "";
        String keyName = GLFW.glfwGetKeyName(keyCode, 0);
        if (keyName != null) return "[" + keyName.toUpperCase() + "]";
        return switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT_SHIFT   -> "[LSHIFT]";
            case GLFW.GLFW_KEY_RIGHT_SHIFT  -> "[RSHIFT]";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "[LCTRL]";
            case GLFW.GLFW_KEY_RIGHT_CONTROL-> "[RCTRL]";
            case GLFW.GLFW_KEY_LEFT_ALT     -> "[LALT]";
            case GLFW.GLFW_KEY_RIGHT_ALT    -> "[RALT]";
            case GLFW.GLFW_KEY_SPACE        -> "[SPACE]";
            case GLFW.GLFW_KEY_TAB          -> "[TAB]";
            case GLFW.GLFW_KEY_CAPS_LOCK    -> "[CAPS]";
            case GLFW.GLFW_KEY_ESCAPE       -> "[ESC]";
            case GLFW.GLFW_KEY_ENTER        -> "[ENTER]";
            case GLFW.GLFW_KEY_BACKSPACE    -> "[BACK]";
            case GLFW.GLFW_KEY_DELETE       -> "[DEL]";
            case GLFW.GLFW_KEY_INSERT       -> "[INS]";
            case GLFW.GLFW_KEY_HOME         -> "[HOME]";
            case GLFW.GLFW_KEY_END          -> "[END]";
            case GLFW.GLFW_KEY_PAGE_UP      -> "[PGUP]";
            case GLFW.GLFW_KEY_PAGE_DOWN    -> "[PGDN]";
            case GLFW.GLFW_KEY_UP           -> "[UP]";
            case GLFW.GLFW_KEY_DOWN         -> "[DOWN]";
            case GLFW.GLFW_KEY_LEFT         -> "[LEFT]";
            case GLFW.GLFW_KEY_RIGHT        -> "[RIGHT]";
            case GLFW.GLFW_KEY_F1  -> "[F1]";  case GLFW.GLFW_KEY_F2  -> "[F2]";
            case GLFW.GLFW_KEY_F3  -> "[F3]";  case GLFW.GLFW_KEY_F4  -> "[F4]";
            case GLFW.GLFW_KEY_F5  -> "[F5]";  case GLFW.GLFW_KEY_F6  -> "[F6]";
            case GLFW.GLFW_KEY_F7  -> "[F7]";  case GLFW.GLFW_KEY_F8  -> "[F8]";
            case GLFW.GLFW_KEY_F9  -> "[F9]";  case GLFW.GLFW_KEY_F10 -> "[F10]";
            case GLFW.GLFW_KEY_F11 -> "[F11]"; case GLFW.GLFW_KEY_F12 -> "[F12]";
            default -> "[" + keyCode + "]";
        };
    }
}