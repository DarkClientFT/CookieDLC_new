package ru.cookiedlc.module.impl.misc;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BindSetting;
import ru.cookiedlc.module.api.setting.implement.GroupSetting;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.api.repository.friend.FriendUtils;
import ru.cookiedlc.common.util.entity.PlayerInventoryUtil;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.event.events.keyboard.KeyEvent;
import ru.cookiedlc.event.events.player.TickEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClickAction extends Module {

    final BindSetting pearlBind = new BindSetting("Pearl Key", "Throw Ender Pearl");
    final BindSetting friendBind = new BindSetting("Friend Add", "Add/Remove Friend");

    final SelectSetting pearlMode = new SelectSetting("Pearl Mode", "Select pearl throw mode")
            .value("Instant", "Legit").selected("Legit");

    final GroupSetting pearlGroup = new GroupSetting("Ender Pearl", "Pearl settings")
            .settings(pearlBind, pearlMode).setValue(true);

    final StopWatch stopWatch = new StopWatch();
    final StopWatch stopWatch2 = new StopWatch();

    ActionType actionType = ActionType.NONE;
    int oldSlot = -1;
    int pearlSlot = -1;
    boolean pearlInOffhand = false;
    boolean pearlFromInventory = false;
    Slot inventorySlot = null;

    public ClickAction() {
        super("ClickAction", "Click Action", ModuleCategory.MISC);
        setup(pearlGroup, friendBind);
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (e.isKeyDown(friendBind.getKey()) && mc.crosshairTarget instanceof EntityHitResult result && result.getEntity() instanceof PlayerEntity player) {
            if (FriendUtils.isFriend(player)) {
                FriendUtils.removeFriend(player);
            } else {
                FriendUtils.addFriend(player);
            }
        }

        if (e.isKeyDown(pearlBind.getKey())) {
            oldSlot = mc.player.getInventory().selectedSlot;

            pearlSlot = findPearlSlot();

            if (pearlSlot == -1 && !pearlInOffhand) {
                return;
            }

            if (pearlMode.isSelected("Legit")) {
                if (actionType == ActionType.NONE) {
                    actionType = ActionType.START;
                    stopWatch.reset();
                    stopWatch2.reset();
                }
            } else {
                instantPearl();
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (actionType != ActionType.NONE) {
            processLegitPearl();
        }
    }

    private void processLegitPearl() {
        switch (actionType) {
            case START -> {
                if (!pearlInOffhand) {
                    mc.player.getInventory().selectedSlot = pearlSlot;
                }
                actionType = ActionType.WAIT;
            }
            case WAIT -> {
                if (stopWatch.every(150)) {
                    actionType = ActionType.USE_ITEM;
                }
            }
            case USE_ITEM -> {
                usePearl();
                actionType = ActionType.SWAP_BACK;
            }
            case SWAP_BACK -> {
                if (stopWatch2.every(450)) {
                    if (!pearlInOffhand) {
                        if (pearlFromInventory && inventorySlot != null) {
                            PlayerInventoryUtil.swapHand(inventorySlot, Hand.MAIN_HAND, false, true);
                        }
                        mc.player.getInventory().selectedSlot = oldSlot;
                    }
                    resetState();
                }
            }
        }
    }

    private void instantPearl() {
        if (!pearlInOffhand) {
            mc.player.getInventory().selectedSlot = pearlSlot;
        }

        usePearl();

        if (!pearlInOffhand) {
            if (pearlFromInventory && inventorySlot != null) {
                PlayerInventoryUtil.swapHand(inventorySlot, Hand.MAIN_HAND, false, true);
            }
            mc.player.getInventory().selectedSlot = oldSlot;
        }

        resetState();
    }

    private void usePearl() {
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
            Hand hand = pearlInOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
            mc.interactionManager.interactItem(mc.player, hand);
        } else {
            rightClick();
        }
    }

    private void rightClick() {
        KeyBinding.onKeyPressed(mc.options.useKey.getDefaultKey());
    }

    private int findPearlSlot() {
        pearlFromInventory = false;
        inventorySlot = null;

        if (mc.player.getOffHandStack().getItem() instanceof EnderPearlItem) {
            pearlInOffhand = true;
            return -1;
        }

        pearlInOffhand = false;

        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof EnderPearlItem) {
                return i;
            }
        }

        Slot slot = PlayerInventoryUtil.getSlot(Items.ENDER_PEARL);
        if (slot != null) {
            inventorySlot = slot;
            pearlFromInventory = true;
            PlayerInventoryUtil.swapHand(slot, Hand.MAIN_HAND, false, false);
            return mc.player.getInventory().selectedSlot;
        }

        return -1;
    }

    private void resetState() {
        actionType = ActionType.NONE;
        pearlSlot = -1;
        pearlInOffhand = false;
        pearlFromInventory = false;
        inventorySlot = null;
    }

    public void disable() {
        resetState();
        oldSlot = -1;
    }

    private enum ActionType {
        NONE, START, WAIT, USE_ITEM, SWAP_BACK
    }
}





            oldSlot = mc.player.getInventory().selectedSlot;

            pearlSlot = findPearlSlot();

            if (pearlSlot == -1 && !pearlInOffhand) {
                return;
            }

            if (pearlMode.isSelected("Legit")) {
                if (actionType == ActionType.NONE) {
                    actionType = ActionType.START;
                    stopWatch.reset();
                    stopWatch2.reset();
                }
            } else {
                instantPearl();
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (actionType != ActionType.NONE) {
            processLegitPearl();
        }
    }

    private void processLegitPearl() {
        switch (actionType) {
            case START -> {
                if (!pearlInOffhand) {
                    mc.player.getInventory().selectedSlot = pearlSlot;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(pearlSlot));
                }
                actionType = ActionType.WAIT;
            }
            case WAIT -> {
                if (stopWatch.every(150)) {
                    actionType = ActionType.USE_ITEM;
                }
            }
            case USE_ITEM -> {
                Hand hand = pearlInOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
                mc.interactionManager.interactItem(mc.player, hand);
                actionType = ActionType.SWAP_BACK;
            }
            case SWAP_BACK -> {
                if (stopWatch2.every(450)) {
                    if (!pearlInOffhand) {
                        mc.player.getInventory().selectedSlot = oldSlot;
                        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
                    }
                    actionType = ActionType.NONE;
                    pearlSlot = -1;
                    pearlInOffhand = false;
                }
            }
        }
    }

    private void instantPearl() {
        if (!pearlInOffhand) {
            mc.player.getInventory().selectedSlot = pearlSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(pearlSlot));
        }

        Hand hand = pearlInOffhand ? Hand.OFF_HAND : Hand.MAIN_HAND;
        mc.interactionManager.interactItem(mc.player, hand);

        if (!pearlInOffhand) {
            mc.player.getInventory().selectedSlot = oldSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(oldSlot));
        }
    }

    private int findPearlSlot() {
        if (mc.player.getOffHandStack().getItem() instanceof EnderPearlItem) {
            pearlInOffhand = true;
            return -1;
        }

        pearlInOffhand = false;

        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof EnderPearlItem) {
                return i;
            }
        }

        Slot slot = PlayerInventoryUtil.getSlot(Items.ENDER_PEARL);
        if (slot != null) {
            PlayerInventoryUtil.swapHand(slot, Hand.MAIN_HAND, false, false);
            return mc.player.getInventory().selectedSlot;
        }

        return -1;
    }

    public void disable() {
        actionType = ActionType.NONE;
        oldSlot = -1;
        pearlSlot = -1;
        pearlInOffhand = false;
    }

    private enum ActionType {
        NONE, START, WAIT, USE_ITEM, SWAP_BACK
    }
}*/
