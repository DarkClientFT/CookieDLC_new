package ru.cookiedlc.module.impl.movement;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.MathHelper;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.event.events.player.SwimmingEvent;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationController;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WaterSpeed extends Module {

    SelectSetting modeSetting = new SelectSetting("Mode", "Select bypass mode").value("FunTime");

    public WaterSpeed() {
        super("WaterSpeed", "Water Speed", ModuleCategory.MOVEMENT);
        setup(modeSetting);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (modeSetting.isSelected("FunTime") && mc.player.isSwimming() && mc.player.isOnGround()) {
            mc.player.jump();
            mc.player.velocity.y = 0.1;
        }
    }

    
    @EventHandler
    public void onSwimming(SwimmingEvent e) {
        if (modeSetting.isSelected("FunTime")) {
            if (mc.options.jumpKey.isPressed()) {
                float pitch = RotationController.INSTANCE.getRotation().getPitch();
                float boost = pitch >= 0 ? MathHelper.clamp(pitch / 45, 1, 2) : 1;
                e.getVector().y = 1 * boost;
            } else if (mc.options.sneakKey.isPressed()) {
                e.getVector().y = -0.8;
            }
        }
    }
}
