package ru.cookiedlc.module.impl.misc.autobuy.holy.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.List;

@Getter
@AllArgsConstructor
public enum EnumItemType {
    EXP_30(Items.EXPERIENCE_BOTTLE, "30", "Бутылочка с 30 ур.", List.of("Бутылек с 30 ур. опыта", "holy-exp-bottle-value:1395")),
    EXP_50(Items.EXPERIENCE_BOTTLE, "50", "Бутылочка с 50 ур.", List.of("Бутылек с 50 ур. опыта (5345)", "holy-exp-bottle-value:5345")),
    EXP_100(Items.EXPERIENCE_BOTTLE, "100", "Бутылочка с 100 ур.", List.of("Бутылек с 100 ур. опыта", "holy-exp-bottle-value:30971")),
    ALMAZ_BLOCK(Items.DIAMOND_BLOCK, "алмазный блок", "Алмазный блок", List.of("Алмазный блок")),
    ALMAZ(Items.DIAMOND, "алмаз", "Алмаз", List.of("Алмаз")),
    NEZER_BLOCK(Items.NETHERITE_BLOCK, "незеритовый блок", "Незеритовый блок", List.of("Незеритовый блок")),
    NEZER_SLITOK(Items.NETHERITE_INGOT, "незеритовый слиток", "Незеритовый слиток", List.of("Незеритовый слиток")),
    POTION_WIN(Items.POTION, "зелье победителя", "Зелье победителя", List.of("Зелье победителя", "kringeItems:{type:\"win-potion\"}")),
    EXPLOSIVE_TRAP(Items.PRISMARINE_SHARD, "взрывная трапка", "Взрывная трапка", List.of("Взрывная трапка", "pyrotechnic-item:{name:\"EXPLOSIVE_TRAP\"}")),
    STAN(Items.NETHER_STAR, "стан", "Стан", List.of("Стан", "pyrotechnic-item:{name:\"STUN_STAR\"}")),
    TNT_A(Items.TNT, "динамит а", "Динамит А", List.of("Динамит A", "pyrotechnic-item:{name:\"A\"}")),
    TNT_B(Items.TNT, "динамит б", "Динамит Б", List.of("Динамит B", "pyrotechnic-item:{name:\"B\"}")),
    TNT_C4(Items.TNT, "динамит с4", "Динамит С4", List.of("pyrotechnic-item:{name:\"C4\"}")),
    TNT_WAVE(Items.TNT, "разрывная волна", "Разрывная волна", List.of("Разрывная волна", "pyrotechnic-item:{name:\"WAVE\"}")),
    TNT_STEALER(Items.TNT, "стиллер", "Стиллер", List.of("Стиллер", "pyrotechnic-item:{name:\"SPAWNER\"}")),
    TNT_LUCKSTEALER(Items.TNT, "надёжный стиллер", "Надежный стиллер", List.of("Надёжный стиллер", "pyrotechnic-item:{name:\"SPAWNER2\"}")),
    TNT_CANNON(Items.DISPENSER, "тнт пушка", "Тнт-Пушка", List.of("Тнт-Пушка", "\"litetntcannon:tnt-cannon\":1b")),
    GUNPOWDER_BLOCK(Items.CLAY, "взрывчатое вещество", "Взрывчатое вещество (блок пороха)", List.of("Взрывчатое вещество", "pyrotechnic-item:{name:\"EXPLOSIVE_SUBSTANCE\"}")),
    GUNPOWDER(Items.GUNPOWDER, "порох", "Порох", List.of()),
    CHARKA(Items.ENCHANTED_GOLDEN_APPLE, "зачарованное золотое яблоко", "Зачарованное золотое яблоко", List.of()),
    GOLDENAPPLE(Items.GOLDEN_APPLE, "золотое яблоко", "Золотое яблоко", List.of()),
    HELMET_SUN(Items.GOLDEN_HELMET, "шлем солнца", "Шлем солнца", List.of("• Шлем солнца •", "kringeItems:{type:\"SunHelmet\"}")),
    HELMET_ETERNITY(Items.NETHERITE_HELMET, "шлем eternity", "Шлем етернити", List.of("- Шлем ᴇᴛᴇʀɴɪᴛʏ -")),
    HELMET_INFINITY(Items.NETHERITE_HELMET, "шлем infinity", "Шлем инфинити", List.of("- Шлем Iɴғɪɴɪᴛʏ -")),
    CHESTPLATE_ETERNITY(Items.NETHERITE_CHESTPLATE, "нагрудник eternity", "Нагрудник етернити", List.of("- Нагрудник ᴇᴛᴇʀɴɪᴛʏ -")),
    CHESTPLATE_INFINITY(Items.NETHERITE_CHESTPLATE, "нагрудник infinity", "Нагрудник инфинити", List.of("- Нагрудник Iɴғɪɴɪᴛʏ -")),
    LEGGINGS_ETERNITY(Items.NETHERITE_LEGGINGS, "штаны eternity", "Поножи етернити", List.of("- Штаны ᴇᴛᴇʀɴɪᴛʏ -")),
    LEGGINGS_INFINITY(Items.NETHERITE_LEGGINGS, "штаны infinity", "Поножи инфинити", List.of("- Поножи Iɴғɪɴɪᴛʏ -")),
    BOOTS_ETERNITY(Items.NETHERITE_BOOTS, "ботинки eternity", "Ботинки етернити", List.of("- Ботинки ᴇᴛᴇʀɴɪᴛʏ -")),
    BOOTS_INFINITY(Items.NETHERITE_BOOTS, "ботинки infinity", "Ботинки инфинити", List.of("- Ботинки Iɴғɪɴɪᴛʏ -")),
    SWORD_ETERNITY(Items.NETHERITE_SWORD, "меч eternity", "Меч етернити", List.of("- Меч ᴇᴛᴇʀɴɪᴛʏ -")),
    PICKAXE_ETERNITY(Items.NETHERITE_PICKAXE, "кирка eternity", "Кирка етернити", List.of("- Кирка ᴇᴛᴇʀɴɪᴛʏ -")),
    SPHERE_CERBER(Items.PLAYER_HEAD, "сфера цербера", "Сфера цербера", List.of("Сфера Цербера", "name:\"Cerber\",rank:\"CERBER\"")),
    SPHERE_FLESHA(Items.PLAYER_HEAD, "сфера флеша", "Сфера флеша", List.of("Сфера Флеша", "name:\"Flash\",rank:\"FLASH\"")),
    SPAWNER(Items.SPAWNER, "Рассадник", "Спавнер", List.of()),
    BACKPACK_1LVL(Items.PINK_SHULKER_BOX, "рюкзак 1 уровень", "Рюкзак (1 ур.)", List.of("Рюкзак (I уровень)", "\"litebackpacks:backpack\":\"mini\"")),
    BACKPACK_2LVL(Items.LIGHT_BLUE_SHULKER_BOX, "рюкзак 2 уровень", "Рюкзак (2 ур.)", List.of("Рюкзак (II уровень)", "\"litebackpacks:backpack\":\"normal\"")),
    BACKPACK_3LVL(Items.RED_SHULKER_BOX, "рюкзак 3 уровень", "Рюкзак (3 ур.)", List.of("Рюкзак (III уровень)", "\"litebackpacks:backpack\":\"big\"")),
    BACKPACK_INFINITY(Items.LIME_SHULKER_BOX, "рюкзак infinity", "Рюкзак Инфинити", List.of("- Рюкзак Iɴғɪɴɪᴛʏ -", "\"litebackpacks:backpack\":\"infinity\"")),
    BACKPACK_4LVL(Items.MAGENTA_SHULKER_BOX, "рюкзак 4 уровень", "Рюкзак (4 ур.)", List.of("Рюкзак (IV уровень)", "\"litebackpacks:backpack\":\"huge\"")),
    ELYTRA(Items.ELYTRA, "элитры", "Элитры", List.of("Элитры")),
    UNBREAKING_ELYTRA(Items.ELYTRA, "нерушимые элитры", "Нерушимая элитра", List.of("Нерушимые элитры", "Unbreakable:1b")),
    ARMOR_ELYTRA(Items.ELYTRA, "броневая элитра", "Броневая элитра", List.of("• Броневая элитра •", "kringeItems:{type:\"ArmorElytra\"}")),
    MYTHIC_DAMAGE3(Items.PLAYER_HEAD, "сфера на урон 3", "Мифическая сфера (у3 / б2)", List.of("Мифическая сфера", "name:\"Mythical1\",rank:\"MYTHICAL\"")),
    MYTHIC_ARMOR3(Items.PLAYER_HEAD, "сфера на броня 3", "Мифическая сфера (б3 / у2)", List.of("Мифическая сфера", "name:\"Mythical2\",rank:\"MYTHICAL\"")),
    MYTHIC_SPEED2(Items.PLAYER_HEAD, "сфера на скорость 2", "Мифическая сфера (б3 / с2)", List.of("Мифическая сфера", "name:\"Mythical3\",rank:\"MYTHICAL\"")),
    SPHERE_SHARD(Items.PLAYER_HEAD, "осколок сферы", "Осколок сферы", List.of("Осколок сферы", "\"magicspheres:burned-sphere-shard\":1b")),
    TOTEM(Items.TOTEM_OF_UNDYING, "тотем бессмертия", "Тотем", List.of()),
    SWORD_ETERNITY_FARMER(Items.NETHERITE_SWORD, "фармер", "Меч етернити (Фармер)", List.of("- Меч ᴇᴛᴇʀɴɪᴛʏ -", "Фармер II")),
    HOE_FARMER(Items.NETHERITE_HOE, "фермер", "Незеритовая мотыга (Фермер)", List.of("Фермер II", "Посев IV")),
    TALISMAN_ETERNITY(Items.TOTEM_OF_UNDYING, "талисман eternity", "Талисман етернити", List.of("- Талисман ᴇᴛᴇʀɴɪᴛʏ -")),
    TALISMAN_INFINITY(Items.TOTEM_OF_UNDYING, "талисман infinity", "Талисман инфинити", List.of("- Талисман ɪɴғɪɴɪᴛʏ -")),
    UNIVERSAL_KEY(Items.TRIPWIRE_HOOK, "универсальный ключ", "Универсальный ключ", List.of("Универсальный ключ", "CustomModelData:123433")),
    LOCKPICK_UNIQUE(Items.TRIPWIRE_HOOK, "отмычка ", "Отмычка (Уникальная)", List.of("[☠] Отмычка", "\"mysticalship:ship-barrel-key\":\"unique\"", "Редкость: Уникальная")),
    LOCKPICK_SECRET(Items.TRIPWIRE_HOOK, "отмычка ", "Отмычка (Секретная)", List.of("[☠] Отмычка", "\"mysticalship:ship-barrel-key\":\"secret\"", "Редкость: Секретная")),
    LOCKPICK_RARE(Items.TRIPWIRE_HOOK, "отмычка ", "Отмычка (Редкая)", List.of("[☠] Отмычка", "\"mysticalship:ship-barrel-key\":\"rare\"", "Редкость: Редкая")),
    LOCKPICK_NORMAL(Items.TRIPWIRE_HOOK, "отмычка ", "Отмычка (Обычная)", List.of("[☠] Отмычка", "\"mysticalship:ship-barrel-key\":\"normal\"", "Редкость: Обычная")),
    JAKE_PICKAXE(Items.GOLDEN_PICKAXE, "золотая кирка джейка", "Золотая кирка Джейка", List.of("Золотая кирка Джейка", "kringeItems:{type:\"jake-pickaxe\"}")),
    MINER_DREAM(Items.NETHERITE_PICKAXE, "мечта шахтера", "Мечта Шахтера", List.of("Мечта Шахтера", "Прочность X")),
    MYSTERY_EGG_CREEPER(Items.CREEPER_SPAWN_EGG, "яйцо призыва крипера", "Загадочное яйцо призыва (Крипер)", List.of("Загадочное яйцо призыва", "SpecialEgg_Pattern:\"first\"")),
    MYSTERY_EGG_WITCH(Items.WITCH_SPAWN_EGG, "яйцо призыва ведьмы", "Загадочное яйцо призыва (Ведьма)", List.of("Загадочное яйцо призыва", "SpecialEgg_Pattern:\"third\"")),
    MYSTERY_EGG_PIGLIN(Items.PIGLIN_BRUTE_SPAWN_EGG, "яйцо призыва брутального пиглина", "Загадочное яйцо призыва (Пиглин)", List.of("Загадочное яйцо призыва", "SpecialEgg_Pattern:\"second\""));
    private final Item itemType;
    private final String searchString;
    private final String name;
    private final List<String> tags;
}
