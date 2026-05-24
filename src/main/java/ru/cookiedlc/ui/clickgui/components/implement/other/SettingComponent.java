package ru.cookiedlc.ui.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.ui.clickgui.components.AbstractComponent;


@Setter
@Accessors(chain = true)
public class SettingComponent extends AbstractComponent {
    private Runnable runnable;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        image.setTexture("textures/settings.png").render(ShapeProperties.create(context.getMatrices(), x, y, 7, 7).color(0xFFafb0bc).build());
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, 7, 7) && button == 0) {
            runnable.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
