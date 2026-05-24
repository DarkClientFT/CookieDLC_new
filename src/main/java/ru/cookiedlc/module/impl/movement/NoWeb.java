package ru.cookiedlc.module.impl.movement;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.event.events.player.TickEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class NoWeb extends Module {
    public static NoWeb getInstance() {
        return Instance.get(NoWeb.class);
    }

    public final SelectSetting webMode = new SelectSetting("Режим", "Выберите режим обхода").value("Grim");

    public NoWeb() {
        super("NoWeb", "No Web", ModuleCategory.MOVEMENT);
        setup(webMode);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (PlayerIntersectionUtil.isPlayerInBlock(Blocks.COBWEB)) {
            double[] speed = Jesus.calculateDirection(0.35);
            mc.player.addVelocity(speed[0], 0, speed[1]);
            mc.player.setVelocity(speed[0], mc.options.jumpKey.isPressed() ? 0.65f : mc.options.sneakKey.isPressed() ? -0.65f : 0, speed[1]);
        }
    }
}