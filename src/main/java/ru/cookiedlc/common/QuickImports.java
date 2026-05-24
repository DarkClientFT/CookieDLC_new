package ru.cookiedlc.common;

import com.google.gson.Gson;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.Window;
import ru.cookiedlc.api.system.draw.DrawEngine;
import ru.cookiedlc.api.system.draw.DrawEngineImpl;
import ru.cookiedlc.api.system.shape.implement.*;
import ru.cookiedlc.ui.clickgui.components.implement.window.WindowManager;

public interface QuickImports extends QuickLogger {
    MinecraftClient mc = MinecraftClient.getInstance();
    RenderTickCounter tickCounter = mc.getRenderTickCounter();
    Window window = mc.getWindow();

    Tessellator tessellator = Tessellator.getInstance();
    DrawEngine drawEngine = new DrawEngineImpl();

    Rectangle rectangle = new Rectangle();
    Blur blur = new Blur();
    Arc arc = new Arc();
    Image image = new Image();

    Gson gson = new Gson();

    WindowManager windowManager = new WindowManager();
}
