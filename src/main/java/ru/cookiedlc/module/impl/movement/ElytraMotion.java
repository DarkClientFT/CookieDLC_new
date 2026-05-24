package ru.cookiedlc.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.common.util.entity.PlayerInventoryUtil;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.module.impl.combat.KillAura;

import java.util.Random;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ElytraMotion extends Module {
    public static Fly getInstance() {
        return Instance.get(Fly.class);
    }
    @NonFinal
    StopWatch timer = new StopWatch();
    @NonFinal Vec3d targetPosition = null;
    @NonFinal
    Random random = new Random();
    @NonFinal double rotationAngle = 0.0;
    public ElytraMotion() {
        super("ElytraMotion", "ElytraMotion", ModuleCategory.MOVEMENT);
        setup(auto,timer2);
    }

    BooleanSetting auto= new BooleanSetting("Авто-фейр","Автоматически использует фейрверк");
    ValueSetting timer2 = new ValueSetting("Скорость исп.", "").setValue(500).range(0F, 10000).visible(()->auto.isValue());

    @EventHandler

    public void onTick(TickEvent e) {
        if (!state || mc.player == null || mc.world == null || !mc.player.isGliding()) return;

        KillAura killAura = Instance.get(KillAura.class);

        if (auto.isValue() && timer.every(timer2.getValue())) {
            PlayerInventoryUtil.swapAndUse(Items.FIREWORK_ROCKET);
            timer.reset();
        }

        if (killAura.isState()) {
            if (killAura.isState() && killAura.getTarget() !=null && mc.player.distanceTo(killAura.getTarget()) < killAura.getAttackRange().getValue() - 0.425F) {
                mc.player.setVelocity(0, 0.02, 0);
            }
        }
    }


    @EventHandler
    public void onPacket(PacketEvent e) {
        KillAura killAura = Instance.get(KillAura.class);
        if (killAura.isState() && killAura.getTarget() != null && mc.player.distanceTo(killAura.getTarget()) < killAura.getAttackRange().getValue() - 0.15F) {
            switch (e.getPacket()) {
                default -> {
                }
            }
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }
}
