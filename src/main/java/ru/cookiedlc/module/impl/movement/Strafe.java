package ru.cookiedlc.module.impl.movement;

import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.Setting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.common.util.entity.MovingUtil;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.event.events.player.MoveEvent;


public class Strafe extends Module {
    private final ValueSetting bpsSetting = (new ValueSetting("Speed", "Strafe Speed")).setValue(0.3F).range(0.3F, 5.0F);

    public static Strafe getInstance() {
        return (Strafe) Instance.get(Strafe.class);
    }

    public Strafe() {
        super("Strafe", "Strafe", ModuleCategory.MOVEMENT);
        this.setup(new Setting[]{this.bpsSetting});
    }

    @EventHandler
    public void onMove(MoveEvent e) {
        if (this.isState() && mc.player != null) {
            if (MovingUtil.hasPlayerMovement()) {
                float finalMultiplier = this.bpsSetting.getValue();
                double[] direction = MovingUtil.calculateDirection((double)finalMultiplier);
                Vec3d currentMovement = e.getMovement();
                Vec3d newMovement = new Vec3d(direction[0], currentMovement.y, direction[1]);
                e.setMovement(newMovement);
            }
        }
    }
}