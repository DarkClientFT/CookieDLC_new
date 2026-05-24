package ru.cookiedlc.ui.clickgui.components.implement.other;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import ru.cookiedlc.api.system.animation.Animation;
import ru.cookiedlc.api.system.animation.Direction;
import ru.cookiedlc.api.system.animation.implement.DecelerateAnimation;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.common.util.render.ScissorManager;
import ru.cookiedlc.common.util.render.Stencil;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.ui.clickgui.components.AbstractComponent;

import static ru.cookiedlc.api.system.animation.Direction.BACKWARDS;
import static ru.cookiedlc.api.system.animation.Direction.FORWARDS;

@Setter
@Accessors(chain = true)
public class CheckComponent extends AbstractComponent {
    private boolean state;
    private Runnable runnable;

    private final Animation alphaAnimation = new DecelerateAnimation()
            .setMs(300)
            .setValue(255);

    private final Animation stencilAnimation = new DecelerateAnimation()
            .setMs(200)
            .setValue(8);


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrix = context.getMatrices();

        alphaAnimation.setDirection(state ? FORWARDS : BACKWARDS);
        stencilAnimation.setDirection(state ? FORWARDS : BACKWARDS);

        int opacity = alphaAnimation.getOutput().intValue();
        
        int borderColor = MathUtil.applyOpacity(0xFFFFFFFF, 45);
        
        rectangle.render(ShapeProperties.create(matrix, x, y, 10, 10)
                .round(3).thickness(0.1F).outlineColor(borderColor).color(0x00000000).build());

        if (state) {
            int iconOnColor = MathUtil.applyOpacity(0xFF00FF00, opacity);
            ru.cookiedlc.api.system.font.Fonts.getSize(12, ru.cookiedlc.api.system.font.Fonts.Type.ICO)
                    .drawString(matrix, "E", x + 1.5F, y + 0.3F, iconOnColor);
        } else {
            int iconOffColor = MathUtil.applyOpacity(0xFFFF0000, 255);
            ru.cookiedlc.api.system.font.Fonts.getSize(10, ru.cookiedlc.api.system.font.Fonts.Type.ICO)
                    .drawString(matrix, "B", x + 2.1F, y + 1F, iconOffColor);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (MathUtil.isHovered(mouseX, mouseY, x, y, 10, 10) && button == 0) {
            runnable.run();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
