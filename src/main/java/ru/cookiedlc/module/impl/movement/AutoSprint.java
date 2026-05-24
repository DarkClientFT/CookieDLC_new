package ru.cookiedlc.module.impl.movement;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.effect.StatusEffects;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.event.events.player.KeepSprintEvent;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.event.events.render.WorldLoadEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoSprint extends Module {
    public static AutoSprint getInstance() {
        return Instance.get(AutoSprint.class);
    }

    public int tickStop = 0;

    private final BooleanSetting keepSprintSetting = new BooleanSetting("Keep Sprint", "Keep sprint before impact, thus not slowing you down")
            .setValue(true);

    public final BooleanSetting ignoreHungerSetting = new BooleanSetting("Ignore Hunger", "Keep sprint before impact, thus not slowing you down")
            .setValue(true);

    public AutoSprint() {
        super("AutoSprint", "Auto Sprint", ModuleCategory.MOVEMENT);
        setup(keepSprintSetting, ignoreHungerSetting);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        tickStop = 3;
    }

    @EventHandler
    public void onTick(TickEvent e) {
        boolean horizontal = mc.player.horizontalCollision && !mc.player.collidedSoftly;
        boolean sneaking = mc.player.isSneaking() && !mc.player.isSwimming();

        if (tickStop > 0 || sneaking) {
            mc.player.setSprinting(false);
        } else if (canStartSprinting() && !horizontal && !mc.options.sprintKey.isPressed()) {
            mc.player.setSprinting(true);
        }
        tickStop--;
    }

    @EventHandler
    public void onKeepSprint(KeepSprintEvent e) {
        if (keepSprintSetting.isValue()) {
            mc.player.setVelocity(mc.player.getVelocity().x / 0.6F, mc.player.getVelocity().y, mc.player.getVelocity().z / 0.6F);
            mc.player.setSprinting(true);
        }
    }

    private boolean canStartSprinting() {
        boolean hasBlindness = mc.player.hasStatusEffect(StatusEffects.BLINDNESS);
        return !mc.player.isSprinting() && mc.player.input.hasForwardMovement() && !hasBlindness && !mc.player.isGliding();
    }
}
