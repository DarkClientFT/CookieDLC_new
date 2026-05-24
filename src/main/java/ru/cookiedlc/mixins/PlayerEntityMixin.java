package ru.cookiedlc.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.cookiedlc.event.api.EventManager;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.event.events.block.PushEvent;
import ru.cookiedlc.event.events.player.KeepSprintEvent;
import ru.cookiedlc.event.events.player.SwimmingEvent;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationController;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements QuickImports {

    @Inject(method = "isPushedByFluids", at = @At("HEAD"), cancellable = true)
    public void isPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        PushEvent event = new PushEvent(PushEvent.Type.WATER);
        EventManager.callEvent(event);
        if (event.isCancelled()) cir.setReturnValue(false);
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setSprinting(Z)V", shift = At.Shift.AFTER))
    public void attackHook(CallbackInfo callbackInfo) {
        EventManager.callEvent(new KeepSprintEvent());
    }

    @ModifyExpressionValue(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getYaw()F"))
    private float hookFixRotation(float original) {
        return RotationController.INSTANCE.getMoveRotation().getYaw();
    }

    @ModifyExpressionValue(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    public Vec3d travelHook(Vec3d vec3d) {
        SwimmingEvent event = new SwimmingEvent(vec3d);
        EventManager.callEvent(event);
        return event.getVector();
    }
}
