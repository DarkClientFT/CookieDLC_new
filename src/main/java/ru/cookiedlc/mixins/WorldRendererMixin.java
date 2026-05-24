package ru.cookiedlc.mixins;

import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.profiler.Profiler;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.event.api.EventManager;
import ru.cookiedlc.event.events.render.WorldRenderEvent;
import ru.cookiedlc.module.impl.render.BlockHighLight;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    protected abstract void renderMain(
            FrameGraphBuilder frameGraphBuilder, Frustum frustum, Camera camera,
            Matrix4f positionMatrix, Matrix4f projectionMatrix, Fog fog,
            boolean renderBlockOutline, boolean hasEntitiesToRender,
            RenderTickCounter renderTickCounter, Profiler profiler
    );

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderMain(Lnet/minecraft/client/render/FrameGraphBuilder;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/render/Fog;ZZLnet/minecraft/client/render/RenderTickCounter;Lnet/minecraft/util/profiler/Profiler;)V")
    )
    private void onRender(
            WorldRenderer instance, FrameGraphBuilder frameGraphBuilder,
            Frustum frustum, Camera camera, Matrix4f positionMatrix,
            Matrix4f projectionMatrix, Fog fog, boolean renderBlockOutline,
            boolean hasEntitiesToRender, RenderTickCounter renderTickCounter,
            Profiler profiler
    ) {
        this.renderMain(frameGraphBuilder, frustum, camera, positionMatrix,
                projectionMatrix, fog, !BlockHighLight.getInstance().isState(),
                hasEntitiesToRender, renderTickCounter, profiler);
    }

    @Inject(
            method = "render",
            at = @At("RETURN")
    )
    private void onWorldRenderPost(
            ObjectAllocator allocator, RenderTickCounter tickCounter,
            boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
            Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci
    ) {
        MatrixStack stack = new MatrixStack();
        stack.multiplyPositionMatrix(positionMatrix);
        WorldRenderEvent event = new WorldRenderEvent(
                stack,
                tickCounter.getTickDelta(true)
        );
        EventManager.callEvent(event);
    }
}