package ru.cookiedlc.module.impl.movement;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Hand;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.api.types.EventType;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.common.util.entity.*;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.common.util.task.scripts.Script;
import ru.cookiedlc.event.events.item.UsingItemEvent;
import ru.cookiedlc.event.events.player.TickEvent;

import net.minecraft.item.CrossbowItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NoSlow extends Module {
    public static NoSlow getInstance() {
        return Instance.get(NoSlow.class);
    }

    private final StopWatch notifWatch = new StopWatch();
    private final Script script = new Script();
    private boolean finish;

    public final SelectSetting itemMode = new SelectSetting("Режим предмета", "Выберите режим обхода").value("Grim Old", "ReallyWorld", "SpookyTime", "Funtime");

    public NoSlow() {
        super("NoSlow", "No Slow", ModuleCategory.MOVEMENT);
        setup(itemMode);
    }

    private int ticks = 0;
    private int cycleCounter = 0;

    private boolean isOnSnowOrCarpet() {
        if (mc.player == null || mc.world == null) return false;
        BlockPos playerPos = mc.player.getBlockPos();
        var block = mc.world.getBlockState(playerPos).getBlock();
        return block == Blocks.SNOW ||
                block == Blocks.WHITE_CARPET ||
                block == Blocks.ORANGE_CARPET ||
                block == Blocks.MAGENTA_CARPET ||
                block == Blocks.LIGHT_BLUE_CARPET ||
                block == Blocks.YELLOW_CARPET ||
                block == Blocks.LIME_CARPET ||
                block == Blocks.PINK_CARPET ||
                block == Blocks.GRAY_CARPET ||
                block == Blocks.LIGHT_GRAY_CARPET ||
                block == Blocks.CYAN_CARPET ||
                block == Blocks.PURPLE_CARPET ||
                block == Blocks.BLUE_CARPET ||
                block == Blocks.BROWN_CARPET ||
                block == Blocks.GREEN_CARPET ||
                block == Blocks.RED_CARPET ||
                block == Blocks.BLACK_CARPET;
    }

    @EventHandler
    public void onUpdate(TickEvent event) {
        if (mc.player == null) return;

        if (itemMode.isSelected("ReallyWorld") || itemMode.isSelected("SpookyTime")) {
            if (!mc.player.isUsingRiptide()) {
                if (mc.player.isUsingItem()) {
                    ticks++;
                } else {
                    ticks = 0;
                    cycleCounter = 0;
                }
            }
        } else {
            if (mc.player.getActiveHand() == Hand.MAIN_HAND || mc.player.getActiveHand() == Hand.OFF_HAND) {
                ticks++;
            } else {
                ticks = 0;
            }
        }
    }

    @EventHandler
    public void onUsingItem(UsingItemEvent e) {
        if (mc.player == null) return;

        Hand first = mc.player.getActiveHand();
        Hand second = first.equals(Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;

        switch (e.getType()) {
            case EventType.ON -> {
                handleItemUse(e, first, second);
            }
            case EventType.POST -> {
                while (!script.isFinished()) script.update();
            }
        }
    }

    private void handleItemUse(UsingItemEvent e, Hand first, Hand second) {
        switch (itemMode.getSelected()) {
            case "Grim Old" -> {
                if (mc.player.getOffHandStack().getUseAction().equals(UseAction.NONE) || mc.player.getMainHandStack().getUseAction().equals(UseAction.NONE)) {
                    PlayerIntersectionUtil.interactItem(first);
                    PlayerIntersectionUtil.interactItem(second);
                    e.cancel();
                }
            }
            case "ReallyWorld" -> {
                int[] thresholds;

                if (mc.options.jumpKey.isPressed()) {
                    thresholds = new int[]{2, 2, 2};
                } else {
                    thresholds = new int[]{2, 3, 3};
                }

                int threshold = thresholds[cycleCounter % thresholds.length];

                if (ticks >= threshold) {
                    e.cancel();
                    ticks = 0;
                    cycleCounter++;
                }
            }
            case "SpookyTime" -> {
                int[] thresholds = new int[]{2, 2, 2};
                int threshold = thresholds[cycleCounter % 2];
                if (ticks >= threshold) {
                    e.cancel();
                    ticks = 0;
                    cycleCounter++;
                }
            }
            case "Funtime" -> {
                if (ticks > 0F && mc.player.getItemUseTime() > 1F) {
                    boolean mainHandCrossbow = mc.player.getMainHandStack().getItem() instanceof CrossbowItem;
                    boolean offHandCrossbow = mc.player.getOffHandStack().getItem() instanceof CrossbowItem;

                    if (mainHandCrossbow || offHandCrossbow) {
                        if (ticks > 0F && mc.player.getItemUseTime() > 1) {
                            e.cancel();
                            ticks = 0;
                        }
                    } else if (mc.player.isOnGround() && isOnSnowOrCarpet()) {
                        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(
                                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
                                mc.player.getBlockPos().up(),
                                Direction.DOWN
                        ));
                        mc.player.setVelocity(
                                mc.player.getVelocity().x,
                                mc.player.getVelocity().y,
                                mc.player.getVelocity().z
                        );
                        e.cancel();
                        ticks = 0;
                    }
                }
            }
        }
    }
}