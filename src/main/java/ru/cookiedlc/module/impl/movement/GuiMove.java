package ru.cookiedlc.module.impl.movement;

import lombok.AccessLevel;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.*;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.AbstractCommandBlockScreen;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.gui.screen.ingame.StructureBlockScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.Vec2f;
import ru.cookiedlc.common.util.entity.PlayerInventoryUtil;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.item.ClickSlotEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.common.util.entity.PlayerInventoryComponent;
import ru.cookiedlc.event.events.container.CloseScreenEvent;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.event.events.player.InputEvent;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.impl.movement.gui.MovementController;

import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GuiMove extends Module {
    private final List<ClickSlotC2SPacket> packets = new ArrayList<>();
    private SlotActionType lastActionType = null;

    public GuiMove() {
        super("GuiMove", "GuiMove", ModuleCategory.MOVEMENT);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (mc.currentScreen instanceof ChatScreen
                || mc.currentScreen instanceof SignEditScreen
                || (isContainerScreenWithoutInventory(mc.currentScreen))) {
            return;
        }

        switch (e.getPacket()) {
            case ClickSlotC2SPacket slot when (!packets.isEmpty() || hasPlayerMovement()) && shouldSkipExecutionGuiMove() -> {
                SlotActionType currentAction = slot.getActionType();

                if (lastActionType != null && !isCompatibleAction(lastActionType, currentAction) && !packets.isEmpty()) {
                    flushPackets();
                }

                packets.add(slot);
                lastActionType = currentAction;
                e.cancel();
            }
            case CloseHandledScreenC2SPacket closePacket when hasPlayerMovement() || !packets.isEmpty() -> {
                e.cancel();
                handleCloseScreen(closePacket);
            }
            case CloseScreenS2CPacket screen when screen.getSyncId() == 0 -> e.cancel();
            default -> {
            }
        }
    }

    private boolean isCompatibleAction(SlotActionType prev, SlotActionType current) {
        boolean prevIsQuickMove = prev == SlotActionType.QUICK_MOVE;
        boolean currentIsQuickMove = current == SlotActionType.QUICK_MOVE;

        boolean prevIsPickup = prev == SlotActionType.PICKUP || prev == SlotActionType.PICKUP_ALL;
        boolean currentIsPickup = current == SlotActionType.PICKUP || current == SlotActionType.PICKUP_ALL;

        if ((prevIsQuickMove && currentIsPickup) || (prevIsPickup && currentIsQuickMove)) {
            return false;
        }

        return true;
    }

    private void flushPackets() {
        if (packets.isEmpty()) return;

        boolean wasSprinting = mc.player.isSprinting();
        boolean wasSneaking = mc.player.isSneaking();

        if (wasSprinting) {
            mc.player.setSprinting(false);
            mc.player.networkHandler.sendPacket(
                    new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING)
            );
        }

        if (wasSneaking) {
            mc.player.networkHandler.sendPacket(
                    new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY)
            );
        }

        for (ClickSlotC2SPacket packet : packets) {
            PlayerInventoryComponent.sendPacketWithOutEvent(packet);
        }
        packets.clear();
        lastActionType = null;

        if (wasSprinting) {
            mc.player.setSprinting(true);
            mc.player.networkHandler.sendPacket(
                    new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING)
            );
        }

        if (wasSneaking) {
            mc.player.networkHandler.sendPacket(
                    new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY)
            );
        }
    }

    private void handleCloseScreen(CloseHandledScreenC2SPacket closePacket) {
        PlayerInventoryComponent.addTask(() -> {
            boolean wasSprinting = mc.player.isSprinting();
            boolean wasSneaking = mc.player.isSneaking();

            if (wasSprinting) {
                mc.player.setSprinting(false);
                mc.player.networkHandler.sendPacket(
                        new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING)
                );
            }

            if (wasSneaking) {
                mc.player.networkHandler.sendPacket(
                        new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY)
                );
            }

            for (ClickSlotC2SPacket packet : packets) {
                PlayerInventoryComponent.sendPacketWithOutEvent(packet);
            }
            packets.clear();
            lastActionType = null;

            PlayerInventoryComponent.sendPacketWithOutEvent(closePacket);

            if (wasSprinting) {
                mc.player.setSprinting(true);
                mc.player.networkHandler.sendPacket(
                        new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING)
                );
            }

            if (wasSneaking) {
                mc.player.networkHandler.sendPacket(
                        new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY)
                );
            }

            PlayerInventoryUtil.updateSlots();
        });
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (!PlayerInventoryUtil.isServerScreen()
                && shouldSkipExecutionGuiMove()
                && (!packets.isEmpty() || hasPlayerMovement())) {
            PlayerInventoryComponent.updateMoveKeys();
        }

        if (!packets.isEmpty() && !hasPlayerMovement() && mc.currentScreen != null) {
            flushPackets();
        }
    }

    @EventHandler
    public void onClickSlot(ClickSlotEvent e) {
        SlotActionType actionType = e.getActionType();
        if ((!packets.isEmpty() || hasPlayerMovement()) &&
                ((e.getButton() == 1 && !actionType.equals(SlotActionType.SWAP) &&
                        !actionType.equals(SlotActionType.THROW)) ||
                        actionType.equals(SlotActionType.PICKUP_ALL))) {
            e.cancel();
        }
    }

    @EventHandler
    public void onCloseScreen(CloseScreenEvent e) {
        if (!packets.isEmpty() && !hasPlayerMovement()) {
            PlayerInventoryComponent.addTask(() -> {
                for (ClickSlotC2SPacket packet : packets) {
                    PlayerInventoryComponent.sendPacketWithOutEvent(packet);
                }
                packets.clear();
                lastActionType = null;
                PlayerInventoryUtil.updateSlots();
            });
        }
    }

    @Override
    public void deactivate() {
        packets.clear();
        lastActionType = null;
        super.deactivate();
    }

    public boolean hasPlayerMovement() {
        return mc.player.input.movementForward != 0f
                || mc.player.input.movementSideways != 0f
                || mc.options.jumpKey.isPressed()
                || mc.player.isSprinting()
                || mc.player.isSneaking();
    }
    public static boolean isContainerScreenWithoutInventory(Screen screen) {
        if (screen == null) {
            return false;
        }

        return screen instanceof CraftingScreen ||
                screen instanceof GenericContainerScreen ||
                screen instanceof FurnaceScreen ||
                screen instanceof BlastFurnaceScreen ||
                screen instanceof SmokerScreen ||
                screen instanceof HopperScreen ||
                screen instanceof ShulkerBoxScreen ||
                screen instanceof BrewingStandScreen ||
                screen instanceof BeaconScreen ||
                screen instanceof AnvilScreen ||
                screen instanceof EnchantmentScreen ||
                screen instanceof CartographyTableScreen ||
                screen instanceof GrindstoneScreen ||
                screen instanceof LoomScreen ||
                screen instanceof StonecutterScreen ||
                screen instanceof SmithingScreen ||
                screen instanceof HorseScreen ||
                screen instanceof MerchantScreen;
    }

    public static boolean isContainerScreen(Screen screen) {
        if (screen == null) {
            return false;
        }

        return screen instanceof InventoryScreen ||
                screen instanceof CraftingScreen ||
                screen instanceof GenericContainerScreen ||
                screen instanceof FurnaceScreen ||
                screen instanceof BlastFurnaceScreen ||
                screen instanceof SmokerScreen ||
                screen instanceof HopperScreen ||
                screen instanceof ShulkerBoxScreen ||
                screen instanceof BrewingStandScreen ||
                screen instanceof BeaconScreen ||
                screen instanceof AnvilScreen ||
                screen instanceof EnchantmentScreen ||
                screen instanceof CartographyTableScreen ||
                screen instanceof GrindstoneScreen ||
                screen instanceof LoomScreen ||
                screen instanceof StonecutterScreen ||
                screen instanceof SmithingScreen ||
                screen instanceof HorseScreen ||
                screen instanceof MerchantScreen;
    }

    public static boolean isMyInventory(Screen screen) {
        if (screen == null) {
            return false;
        }

        return screen instanceof InventoryScreen ||
                screen instanceof CreativeInventoryScreen;
    }

    public boolean shouldSkipExecutionGuiMove() {
        return mc.currentScreen != null && !isChatOpen() && !(mc.currentScreen instanceof SignEditScreen) && !(mc.currentScreen instanceof AnvilScreen)
                && !(mc.currentScreen instanceof AbstractCommandBlockScreen) && !(mc.currentScreen instanceof StructureBlockScreen);
    }

    public static boolean isChatOpen() {
        return mc.currentScreen instanceof ChatScreen;
    }
}
