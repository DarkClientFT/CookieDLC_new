package ru.cookiedlc.ui.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import ru.cookiedlc.api.system.font.Fonts;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.ui.clickgui.components.implement.window.AbstractWindow;
import ru.cookiedlc.ui.clickgui.MenuScreen;
import ru.cookiedlc.ui.clickgui.components.AbstractComponent;

import java.util.Optional;

import static ru.cookiedlc.api.system.font.Fonts.Type.BOLD;
import static ru.cookiedlc.api.system.font.Fonts.Type.ICO;

@Setter
@Accessors(chain = true)
public class BackgroundComponent extends AbstractComponent {

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        rectangle.render(ShapeProperties.create(matrix, x, y, width, height).round(6).softness(1).thickness(2).quality(50)
                .outlineColor(ColorUtil.getOutline()).color(ColorUtil.getMainGuiColor()).build());

        rectangle.render(ShapeProperties.create(context.getMatrices(), x + 85, y, 0.5F, height)
                .color(ColorUtil.getOutline(0.5F, 1)).build());
        rectangle.render(ShapeProperties.create(context.getMatrices(), x + 85.5F, y + 28, width - 85.5F, 0.5F)
                .color(ColorUtil.getOutline(0.5F, 1)).build());

       Fonts.getSize(16).drawString(matrix, MenuScreen.INSTANCE.getCategory().getReadableName(), x + 95, y + 13, 0xFFD4D6E1);
    }
}