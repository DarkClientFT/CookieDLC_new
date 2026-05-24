package ru.cookiedlc.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.BowItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.event.events.player.TickEvent;


@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BowSpammer extends Module {

    final ValueSetting delay = new ValueSetting("Задержка", "Задержка между выстрелами")
            .range(2.2f, 5.0f).setValue(2.5f);

    public BowSpammer() {
        super("BowSpammer", "Bow Spammer", ModuleCategory.COMBAT);
        setup(delay);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) return;
        if (mc.getNetworkHandler() == null) return;

        if (!canShoot()) return;

        sendShootPackets();
        mc.player.stopUsingItem();
    }

    private boolean canShoot() {
        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) return false;
        if (!mc.player.isUsingItem()) return false;
        return mc.player.getItemUseTime() >= delay.getValue();
    }

    private void sendShootPackets() {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.RELEASE_USE_ITEM,
                BlockPos.ORIGIN,
                Direction.DOWN
        ));

        mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(
                Hand.MAIN_HAND,
                0,
                mc.player.getYaw(),
                mc.player.getPitch()
        ));
    }
}