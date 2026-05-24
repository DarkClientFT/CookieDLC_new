package ru.cookiedlc.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.ui.hud.api.AbstractDraggable;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.module.impl.render.Hud;

import java.util.List;

@Mixin(ChatScreen.class)
public class ChatScreenMixin extends Screen implements QuickImports {
    @Unique
    List<AbstractDraggable> draggables = Main.getInstance().getDraggableRepository().draggable();

    protected ChatScreenMixin() {
        super(Text.empty());
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Hud hud = Hud.getInstance();
        draggables.stream().filter(draggable -> draggable.canDraw(hud, draggable) && draggable.isDragging())
                .reduce((first, second) -> second)
                .ifPresent(active -> draggables.forEach(draggable -> {if (active == draggable) draggable.render(context, mouseX, mouseY, delta);}));
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"))
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        draggables.forEach(draggable -> draggable.mouseClicked(mouseX, mouseY, button));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggables.forEach(draggable -> draggable.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
