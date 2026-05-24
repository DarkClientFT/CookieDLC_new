package ru.cookiedlc.mixins;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.cookiedlc.event.api.EventManager;
import ru.cookiedlc.event.events.container.HandledScreenEvent;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin1 {

    @Shadow public int backgroundWidth;
    @Shadow public int backgroundHeight;
    @Shadow public int x;
    @Shadow public int y;

    @Shadow
    @Nullable
    protected Slot focusedSlot;


    @Inject(method = "render", at = @At("RETURN"))
    public void renderHook(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        EventManager.callEvent(new HandledScreenEvent(context, focusedSlot, backgroundWidth, backgroundHeight));
    }
}