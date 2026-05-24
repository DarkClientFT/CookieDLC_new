package ru.cookiedlc.mixins;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.cookiedlc.module.impl.render.NoEffects;

@Mixin(LivingEntity.class)
public class NoEffectsMixin {

    @Inject(method = "hasStatusEffect", at = @At("HEAD"), cancellable = true)
    private void onHasStatusEffect(RegistryEntry<StatusEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        NoEffects module = NoEffects.getInstance();
        if (module == null || !module.isEnabled()) return;

        if (effect.matches(StatusEffects.NIGHT_VISION) && module.modeListSetting.isSelected("Ночное зрение")) {
            cir.setReturnValue(false);
        }
        if (effect.matches(StatusEffects.DARKNESS) && module.modeListSetting.isSelected("Тьма")) {
            cir.setReturnValue(false);
        }
        if (effect.matches(StatusEffects.BLINDNESS) && module.modeListSetting.isSelected("Слепота")) {
            cir.setReturnValue(false);
        }
        if (effect.matches(StatusEffects.NAUSEA) && module.modeListSetting.isSelected("Тошнота")) {
            cir.setReturnValue(false);
        }
        if (effect.matches(StatusEffects.GLOWING) && module.modeListSetting.isSelected("Свечение")) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        NoEffects module = NoEffects.getInstance();
        if (module != null && module.isEnabled() && module.modeListSetting.isSelected("Свечение")) {
            cir.setReturnValue(false);
        }
    }
}