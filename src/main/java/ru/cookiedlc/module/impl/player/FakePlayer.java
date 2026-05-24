package ru.cookiedlc.module.impl.player;

import com.mojang.authlib.GameProfile;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.module.api.setting.implement.TextSetting;
import ru.cookiedlc.event.events.player.AttackEvent;
import ru.cookiedlc.event.events.player.TickEvent;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FakePlayer extends Module {

    static int FAKE_PLAYER_ID = -49204;

    SelectSetting armorType = new SelectSetting("Armor", "Тип брони")
            .value("Diamond", "Netherite");

    TextSetting botName = new TextSetting("Name", "Никнейм фейк игрока")
            .setText("FakePlayer")
            .setMax(16);

    @NonFinal
    OtherClientPlayerEntity fakePlayer = null;

    public FakePlayer() {
        super("FakePlayer", "Fake Player", ModuleCategory.PLAYER);
        setup(armorType, botName);
    }

    @Override
    public void activate() {
        if (mc.player == null || mc.world == null) {
            setState(false);
            return;
        }

        UUID botUuid = UUID.randomUUID();
        GameProfile gameProfile = new GameProfile(botUuid, botName.getText());

        fakePlayer = new OtherClientPlayerEntity(mc.world, gameProfile);
        fakePlayer.copyPositionAndRotation(mc.player);
        fakePlayer.headYaw = mc.player.headYaw;
        fakePlayer.setId(FAKE_PLAYER_ID);

        equipArmor();

        fakePlayer.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        fakePlayer.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        mc.world.addEntity(fakePlayer);
        fakePlayer.setHealth(20f);
    }

    @Override
    public void deactivate() {
        if (fakePlayer != null && mc.world != null) {
            mc.world.removeEntity(fakePlayer.getId(), Entity.RemovalReason.DISCARDED);
        }
        fakePlayer = null;
    }

    private void equipArmor() {
        if (fakePlayer == null) return;

        Item helmet, chestplate, leggings, boots;

        if (armorType.isSelected("Netherite")) {
            helmet = Items.NETHERITE_HELMET;
            chestplate = Items.NETHERITE_CHESTPLATE;
            leggings = Items.NETHERITE_LEGGINGS;
            boots = Items.NETHERITE_BOOTS;
        } else {
            helmet = Items.DIAMOND_HELMET;
            chestplate = Items.DIAMOND_CHESTPLATE;
            leggings = Items.DIAMOND_LEGGINGS;
            boots = Items.DIAMOND_BOOTS;
        }

        ItemStack helmetStack = makeUnbreakable(new ItemStack(helmet));
        ItemStack chestplateStack = makeUnbreakable(new ItemStack(chestplate));
        ItemStack leggingsStack = makeUnbreakable(new ItemStack(leggings));
        ItemStack bootsStack = makeUnbreakable(new ItemStack(boots));

        fakePlayer.equipStack(EquipmentSlot.HEAD, helmetStack);
        fakePlayer.equipStack(EquipmentSlot.CHEST, chestplateStack);
        fakePlayer.equipStack(EquipmentSlot.LEGS, leggingsStack);
        fakePlayer.equipStack(EquipmentSlot.FEET, bootsStack);
    }

    private ItemStack makeUnbreakable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return stack;
        stack.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
        return stack;
    }

    @EventHandler
    public void onAttack(AttackEvent e) {
        if (fakePlayer == null || e.getEntity() != fakePlayer) return;
        if (fakePlayer.hurtTime > 0) return;

        if (mc.player.fallDistance > 0) {
            mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1f, 1f);
        } else {
            mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(),
                    SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1f, 1f);
        }

        fakePlayer.onDamaged(mc.world.getDamageSources().generic());

        float damage = mc.player.getAttackCooldownProgressPerTick() >= 0.85f ? 1f : 0.5f;
        fakePlayer.setHealth(fakePlayer.getHealth() + fakePlayer.getAbsorptionAmount() - damage);

        if (fakePlayer.getHealth() <= 0) {
            fakePlayer.setHealth(20f);
            applyTotemEffects();
            mc.world.playSound(mc.player, fakePlayer.getX(), fakePlayer.getY(), fakePlayer.getZ(),
                    SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 1f, 1f);
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (fakePlayer == null || mc.player == null || mc.world == null) return;

        applyTotemEffects();

        if (fakePlayer.getOffHandStack().getItem() != Items.TOTEM_OF_UNDYING) {
            fakePlayer.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.TOTEM_OF_UNDYING));
        }
    }

    private void applyTotemEffects() {
        if (fakePlayer == null) return;
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 99999, 3));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 99999, 9));
        fakePlayer.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 99999, 1));
    }
}
