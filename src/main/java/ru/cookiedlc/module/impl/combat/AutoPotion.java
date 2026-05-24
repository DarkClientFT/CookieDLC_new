package ru.cookiedlc.module.impl.combat;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.entity.PlayerInventoryUtil;
import ru.cookiedlc.common.util.world.ServerUtil;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.api.types.EventType;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.common.util.task.TaskPriority;
import ru.cookiedlc.event.events.player.RotationUpdateEvent;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.api.setting.implement.MultiSelectSetting;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class AutoPotion extends Module {
    public final MultiSelectSetting potions = new MultiSelectSetting("Бафать", "Выберите зелья для использования")
            .value("Силу", "Скорость", "Огнестойкость");
    private final BooleanSetting autoDisable = new BooleanSetting("Авто выключение", "Автоматически выключается после использования всех зелий").setValue(false);
    private final BooleanSetting onlyPvP = new BooleanSetting("Только в PVP", "Использовать зелья только в PVP").setValue(false);

    private boolean isActive;
    private int selectedSlot = -2;
    private float previousPitch;
    private final StopWatch time = new StopWatch();
    private int previousSlot = -1;

    public AutoPotion() {
        super("AutoPotion", "AutoPotion", ModuleCategory.PLAYER);
        setup(potions, onlyPvP, autoDisable);
    }

    public boolean isActivePotion;

    @EventHandler
    public void onTick(TickEvent e) {
        if (PlayerIntersectionUtil.nullCheck()) return;

        if (mc.player.isGliding() || (mc.player.isUsingItem() && !mc.player.isBlocking())) return;

        if (this.isState() && this.shouldUsePotion()) {
            boolean anyActive = false;
            for (PotionType potionType : PotionType.values()) {
                if (potionType.isEnabled()) {
                    anyActive = true;
                    break;
                }
            }
            isActivePotion = anyActive;
        } else {
            isActivePotion = false;
        }

        if (this.isState() && this.shouldUsePotion() && previousPitch == mc.player.getPitch()) {
            int oldItem = mc.player.getInventory().selectedSlot;
            this.selectedSlot = -1;

            for (PotionType potionType : PotionType.values()) {
                if (potions.isSelected(potionType.getSettingName()) && potionType.isEnabled()) {
                    int slot = this.findPotionSlot(potionType);
                    if (this.selectedSlot == -1 && slot != -1) {
                        this.selectedSlot = slot;
                    }
                    this.isActive = true;
                }
            }

            if (this.selectedSlot != -1 && this.selectedSlot < 9) {
                if (previousSlot == -1) {
                    previousSlot = oldItem;
                }
                mc.player.getInventory().selectedSlot = this.selectedSlot;
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(this.selectedSlot));
                }
                PlayerIntersectionUtil.interactItem(Hand.MAIN_HAND);
            }
        }

        if (time.finished(500)) {
            try {
                this.reset();
                this.selectedSlot = -2;
                if (previousSlot != -1) {
                    mc.player.getInventory().selectedSlot = previousSlot;
                    if (mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
                    }
                    previousSlot = -1;
                }
            } catch (Exception ignored) {
            }
        }

        if (this.autoDisable.isValue() && this.isActive && this.selectedSlot == -2) {
            this.setState(false);
            this.isActive = false;
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() != 0) return;
        if (!this.isState() || !this.shouldUsePotion()) return;
        if (PlayerIntersectionUtil.nullCheck()) return;

        if (mc.player != null) {
            this.previousPitch = mc.player.getPitch();
        }
    }

    private boolean shouldUsePotion() {
        if (onlyPvP.isValue()) {
            return ServerUtil.isPvp();
        }
        return true;
    }

    private void reset() {
        for (PotionType potionType : PotionType.values()) {
            if (potions.isSelected(potionType.getSettingName())) {
                potionType.setEnabled(this.isPotionActive(potionType));
            }
        }
    }

    private int findPotionSlot(PotionType type) {

        int hbSlot = this.getPotionIndexHotbar(type.getPotion());
        if (hbSlot != -1) {
            type.setEnabled(false);
            time.reset();
            return hbSlot;
        }


        int invSlot = this.getPotionIndexInventory(type.getPotion());
        if (invSlot != -1) {

            int hotbarSlot = findFreeHotbarSlot();
            if (hotbarSlot != -1) {
                swapSlots(invSlot, hotbarSlot);
                type.setEnabled(false);
                time.reset();
                return hotbarSlot;
            }
        }

        return -1;
    }
    public static int findFreeHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) {
                return i;
            }
        }
        return 8;
    }
    public static void swapSlots(int fromSlot, int toSlot) {
        if (mc.interactionManager == null || mc.player.currentScreenHandler == null) return;


        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                fromSlot,
                toSlot,
                SlotActionType.SWAP,
                mc.player
        );
    }
    private int getPotionIndexHotbar(RegistryEntry<StatusEffect> effect) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.SPLASH_POTION) {
                PotionContentsComponent component = stack.get(DataComponentTypes.POTION_CONTENTS);
                if (component != null) {
                    for (StatusEffectInstance potionEffect : component.getEffects()) {
                        if (potionEffect.getEffectType().equals(effect)) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private int getPotionIndexInventory(RegistryEntry<StatusEffect> effect) {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.SPLASH_POTION) {
                PotionContentsComponent component = stack.get(DataComponentTypes.POTION_CONTENTS);
                if (component != null) {
                    for (StatusEffectInstance potionEffect : component.getEffects()) {
                        if (potionEffect.getEffectType().equals(effect)) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public boolean isActive() {
        for (PotionType potionType : PotionType.values()) {
            if (potions.isSelected(potionType.getSettingName()) && potionType.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPotionActive(PotionType type) {
        if (PlayerIntersectionUtil.isPotionActive(type.getPotion())) {
            this.isActive = false;
            return false;
        } else {
            return this.getPotionIndexInventory(type.getPotion()) != -1 ||
                    this.getPotionIndexHotbar(type.getPotion()) != -1;
        }
    }

    @Override
    public void deactivate() {
        isActive = false;
        selectedSlot = -2;
        previousSlot = -1;
        super.deactivate();
    }

    enum PotionType {
        STRENGTH(StatusEffects.STRENGTH, "Силу"),
        SPEED(StatusEffects.SPEED, "Скорость"),
        FIRE_RESIST(StatusEffects.FIRE_RESISTANCE, "Огнестойкость");

        private final RegistryEntry<StatusEffect> potion;
        private final String settingName;
        private boolean enabled;

        PotionType(RegistryEntry<StatusEffect> potion, String settingName) {
            this.potion = potion;
            this.settingName = settingName;
        }

        public RegistryEntry<StatusEffect> getPotion() {
            return this.potion;
        }

        public String getSettingName() {
            return this.settingName;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
