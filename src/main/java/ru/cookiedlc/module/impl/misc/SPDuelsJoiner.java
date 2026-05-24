package ru.cookiedlc.module.impl.misc;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.event.events.player.TickEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SPDuelsJoiner extends Module {

    enum State { SELECT_SLOT, OPEN_MENU, CLICK_JOIN, WAIT_DIAMOND, DONE }

    final StopWatch actionCooldown = new StopWatch();
    State state = State.SELECT_SLOT;

    public SPDuelsJoiner() {
        super("SPDuelsJoiner", "Spooky Joiner", ModuleCategory.MISC);
    }

    @Override

    public void activate() {
        super.activate();
        state = State.SELECT_SLOT;
        actionCooldown.reset();
    }

    @EventHandler

    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) return;
        long handle = mc.getWindow().getHandle();
        if (GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            state = State.DONE;
            this.deactivate();
            return;
        }

        switch (state) {
            case SELECT_SLOT -> {
                int targetSlot = 4;
                ensureSelectedSlot(targetSlot);
                state = State.OPEN_MENU;
                actionCooldown.reset();
            }
            case OPEN_MENU -> {
                ensureSelectedSlot(4);
                if (isSixRowContainerOpen()) {
                    state = State.CLICK_JOIN;
                    actionCooldown.reset();
                    break;
                }
                if (actionCooldown.finished(150)) {
                    PlayerIntersectionUtil.interactItem(Hand.MAIN_HAND);
                    actionCooldown.reset();
                }
            }
            case CLICK_JOIN -> {
                ensureSelectedSlot(4);
                if (!isSixRowContainerOpen()) {
                    state = State.OPEN_MENU;
                    actionCooldown.reset();
                    break;
                }
                if (actionCooldown.finished(150)) {
                    clickMenuSlot(14);
                    actionCooldown.reset();
                }
                state = State.WAIT_DIAMOND;
            }
            case WAIT_DIAMOND -> {
                ensureSelectedSlot(4);
                if (mc.player.getInventory().getStack(0).isOf(Items.DIAMOND_SWORD)) {
                    if (mc.player.currentScreenHandler != null && mc.currentScreen != null) {
                        mc.player.closeHandledScreen();
                    }
                    state = State.DONE;
                    this.deactivate();
                    break;
                }
                if (isSixRowContainerOpen() && actionCooldown.finished(200)) {
                    clickMenuSlot(14);
                    actionCooldown.reset();
                }
                if (!isSixRowContainerOpen()) {
                    state = State.OPEN_MENU;
                    actionCooldown.reset();
                }
            }
            case DONE -> {
                this.setState(false);
            }
        }
    }

    private boolean isSixRowContainerOpen() {
        if (!(mc.currentScreen instanceof HandledScreen<?> handled)) return false;
        ScreenHandler handler = handled.getScreenHandler();
        return handler != null && handler.slots.size() >= 54;
    }

    private void clickMenuSlot(int slotIndex) {
        if (!(mc.currentScreen instanceof HandledScreen<?> handled)) return;
        ScreenHandler handler = handled.getScreenHandler();
        if (handler == null) return;
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return;
        mc.interactionManager.clickSlot(handler.syncId, slotIndex, 0, SlotActionType.PICKUP, mc.player);
    }

    private void ensureSelectedSlot(int idx) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.getInventory().selectedSlot != idx) {
            mc.player.getInventory().selectedSlot = idx;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(idx));
        }
    }
}