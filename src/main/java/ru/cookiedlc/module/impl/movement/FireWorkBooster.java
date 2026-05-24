package ru.cookiedlc.module.impl.movement;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.event.events.player.FireworkEvent;
import ru.cookiedlc.module.impl.combat.killaura.rotation.RotationController;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FireWorkBooster extends Module {
    SelectSetting modeSetting = new SelectSetting("Mode", "Selects the type of mode")
            .value("Grim");

    public FireWorkBooster() {
        super("FireWorkBooster", "FireWork Booster", ModuleCategory.MOVEMENT);
        setup(modeSetting);
    }

    
    @EventHandler
    public void onFirework(FireworkEvent e) {
        if (modeSetting.isSelected("Grim")) {
            int ff = RotationController.INSTANCE.getRotation().getYaw() > 0F ? 45 : -45;
            double acceleration = Math.abs((RotationController.INSTANCE.getRotation().getYaw() + ff) % 90 - ff) / 45, boost = 1 + (0.3 * acceleration * acceleration);
            boolean yAcceleration = Math.abs(RotationController.INSTANCE.getMoveRotation().getPitch()) > 60;
            Vec3d vec3d = e.getVector();
            e.setVector(new Vec3d(vec3d.x * boost, yAcceleration ? vec3d.y * boost : vec3d.y, vec3d.z * boost));
        }
    }
}
