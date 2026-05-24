package ru.cookiedlc.mixins;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.Icons;
import net.minecraft.client.util.MacWindowUtil;
import net.minecraft.client.util.Window;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.cookiedlc.api.auth.AuthData;
import ru.cookiedlc.event.api.EventManager;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.common.util.other.BufferUtil;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.api.file.exception.FileProcessingException;
import ru.cookiedlc.api.system.font.Fonts;
import ru.cookiedlc.common.util.logger.LoggerUtil;
import ru.cookiedlc.event.events.container.SetScreenEvent;
import ru.cookiedlc.event.events.player.HotBarUpdateEvent;
import ru.cookiedlc.module.impl.combat.NoInteract;

import java.io.IOException;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements QuickImports {

    @Shadow @Nullable public abstract ClientPlayNetworkHandler getNetworkHandler();

    @Shadow @Nullable public ClientPlayerInteractionManager interactionManager;

    @Shadow @Nullable public ClientPlayerEntity player;

    @Shadow @Final public GameRenderer gameRenderer;

    @Shadow @Nullable public Screen currentScreen;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void onInit(RunArgs args, CallbackInfo ci) {
        Fonts.init();
    }

    @Inject(at = @At("HEAD"), method = "stop")
    private void stop(CallbackInfo ci) {
        LoggerUtil.info("Stopping for MinecraftClient");
        if (Main.getInstance().isInitialized()) {
            try {
                Main.getInstance().getFileController().saveFiles();
            } catch (FileProcessingException e) {
                LoggerUtil.error("Error occurred while saving files: " + e.getMessage() + " " + e.getCause());
            } finally {
                Main.getInstance().getFileController().stopAutoSave();
            }
        }
    }
    @Inject(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Hand;values()[Lnet/minecraft/util/Hand;"), cancellable = true)
    public void doItemUseHook(CallbackInfo ci) {
       if (NoInteract.getInstance().isState()) {
           for (Hand hand : Hand.values()) {
               if (player.getStackInHand(hand).isEmpty()) continue;
               ActionResult result = interactionManager.interactItem(player, hand);
               if (result.isAccepted()) {
                   if (result instanceof ActionResult.Success success && success.swingSource().equals(ActionResult.SwingSource.CLIENT)) {
                       gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
                       player.swingHand(hand);
                   }
                   ci.cancel();
               }
           }
       }
    }

    @Inject(method = "setScreen", at = @At(value = "HEAD"), cancellable = true)
    public void setScreenHook(Screen screen, CallbackInfo ci) {
        SetScreenEvent event = new SetScreenEvent(screen);
        EventManager.callEvent(event);
        Main.getInstance().getDraggableRepository().draggable().forEach(drag -> drag.setScreen(event));
        Screen eventScreen = event.getScreen();
        if (screen != eventScreen) {
            mc.setScreen(eventScreen);
            ci.cancel();
        }
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setIcon(Lnet/minecraft/resource/ResourcePack;Lnet/minecraft/client/util/Icons;)V"))
    private void onChangeIcon(Window instance, ResourcePack resourcePack, Icons icons) throws IOException {
        if (GLFW.glfwGetPlatform() == 393218) {
            MacWindowUtil.setApplicationIconImage(icons.getMacIcon(resourcePack));
            return;
        }

        BufferUtil.setWindowIcon(Main.class.getResourceAsStream("/cookie.png"), Main.class.getResourceAsStream("/cookie.png"));
    }

    @Inject(method = "getWindowTitle", at = @At("HEAD"), cancellable = true)
    private void onWindowTitle(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue("CookieDLC » " + AuthData.getInstance().getUsername() + " « " + SharedConstants.getGameVersion().getName());
    }

    @Inject(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;"), cancellable = true)
    public void handleInputEventsHook(CallbackInfo ci) {
        HotBarUpdateEvent event = new HotBarUpdateEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) ci.cancel();
    }
}
