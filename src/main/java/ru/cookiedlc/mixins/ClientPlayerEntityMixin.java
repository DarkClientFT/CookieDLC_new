package ru.cookiedlc.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.cookiedlc.event.api.EventManager;
import ru.cookiedlc.event.api.types.EventType;
import ru.cookiedlc.common.util.entity.PlayerInventoryComponent;
import ru.cookiedlc.event.events.block.PushEvent;
import ru.cookiedlc.event.events.container.CloseScreenEvent;
import ru.cookiedlc.event.events.item.UsingItemEvent;
import ru.cookiedlc.event.events.player.*;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationController;
import ru.cookiedlc.module.impl.movement.AutoSprint;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow
    public abstract float getPitch(float tickDelta);

    @Shadow
    public abstract float getYaw(float tickDelta);

    @Final
    @Shadow
    protected MinecraftClient client;

    @Shadow protected abstract void autoJump(float dx, float dz);

    @Shadow public Input input;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo info) {
        if (client.player != null && client.world != null) {
            EventManager.callEvent(new TickEvent());
        }
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tick()V", shift = At.Shift.AFTER))
    public void postTick(CallbackInfo callbackInfo) {
        if (client.player != null && client.world != null) {
            EventManager.callEvent(new PostTickEvent());
        }
    }

    @ModifyExpressionValue(method = {"sendMovementPackets", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F"))
    private float hookSilentRotationYaw(float original) {
        return RotationController.INSTANCE.getRotation().getYaw();
    }

    @ModifyExpressionValue(method = {"sendMovementPackets", "tick"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F"))
    private float hookSilentRotationPitch(float original) {
        return RotationController.INSTANCE.getRotation().getPitch();
    }

    @Inject(method = "closeHandledScreen", at = @At(value = "HEAD"), cancellable = true)
    private void closeHandledScreenHook(CallbackInfo info) {
        CloseScreenEvent event = new CloseScreenEvent(client.currentScreen);
        EventManager.callEvent(event);
        if (event.isCancelled()) info.cancel();
    }

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean usingItemHook(boolean original) {
        if (original) {
            UsingItemEvent event = new UsingItemEvent(EventType.ON);
            EventManager.callEvent(event);
            if (event.isCancelled()) return false;
            AutoSprint.getInstance().tickStop = 1;
        }
        return original;
    }

    @WrapOperation(method = "canSprint", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;getFoodLevel()I"))
    private int canSprintHook(HungerManager instance, Operation<Integer> original) {
        AutoSprint autoSprint = AutoSprint.getInstance();
        return autoSprint.isState() && autoSprint.ignoreHungerSetting.isValue() ? 20 : original.call(instance);
    }

    @Inject(method = "sendMovementPackets", at = @At(value = "HEAD"), cancellable = true)
    private void preMotion(CallbackInfo ci) {
        MotionEvent event = new MotionEvent(getX(), getY(), getZ(), getYaw(1), getPitch(1), isOnGround());
        EventManager.callEvent(event);
        if (event.isCancelled()) ci.cancel();
    }

    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V"), cancellable = true)
    public void onMoveHook(MovementType movementType, Vec3d movement, CallbackInfo ci) {
        MoveEvent event = new MoveEvent(movement);
        EventManager.callEvent(event);
        double d = this.getX();
        double e = this.getZ();
        super.move(movementType, event.getMovement());
        this.autoJump((float) (this.getX() - d), (float) (this.getZ() - e));
        ci.cancel();
    }

    @Inject(method = "sendMovementPackets", at = @At("RETURN"), cancellable = true)
    private void postMotion(CallbackInfo ci) {
        PostMotionEvent postMotionEvent = new PostMotionEvent();
        EventManager.callEvent(postMotionEvent);
        PlayerInventoryComponent.postMotion();
        if (postMotionEvent.isCancelled()) ci.cancel();
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
        PushEvent event = new PushEvent(PushEvent.Type.BLOCK);
        EventManager.callEvent(event);
        if (event.isCancelled()) ci.cancel();
    }


}
