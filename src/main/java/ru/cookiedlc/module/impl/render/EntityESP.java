package ru.cookiedlc.module.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector4d;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.api.repository.friend.FriendUtils;
import ru.cookiedlc.api.system.font.FontRenderer;
import ru.cookiedlc.api.system.font.Fonts;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.entity.PlayerIntersectionUtil;
import ru.cookiedlc.common.util.math.ProjectionUtil;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.common.util.render.Render2DUtil;
import ru.cookiedlc.common.util.render.Render3DUtil;
import ru.cookiedlc.common.util.world.ServerUtil;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.event.events.render.DrawEvent;
import ru.cookiedlc.event.events.render.WorldLoadEvent;
import ru.cookiedlc.event.events.render.WorldRenderEvent;
import ru.cookiedlc.module.impl.combat.AntiBot;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.MultiSelectSetting;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;

import java.util.*;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = false)
public class EntityESP extends Module {

    private static final Identifier TEXTURE = Identifier.of("textures/container.png");
    private static final int SHULKER_WIDTH = 176, SHULKER_HEIGHT = 67;

    List<PlayerEntity> players = new ArrayList<>(16);
    List<ItemStack> armorCache = new ArrayList<>(4);
    Map<RegistryKey<Enchantment>, String> encMap;
    Vector4d tempVec = new Vector4d();

    Map<RegistryKey<Enchantment>, RegistryEntry<Enchantment>> enchantCache = new HashMap<>();
    boolean enchantCacheValid = false;

    ValueSetting sizeSetting = new ValueSetting("Tag Size", "Tags size").setValue(13).range(10, 20);
    public MultiSelectSetting entityType = new MultiSelectSetting("Entity Type", "Entity that will be displayed")
            .value("Player", "Item", "TNT");
    MultiSelectSetting playerSetting = new MultiSelectSetting("Player Settings", "Settings for players")
            .value("Box", "Armor", "Enchants", "NameTags", "Hand Items").visible(() -> entityType.isSelected("Player"));
    public SelectSetting boxType = new SelectSetting("Box Type", "Type of Box")
            .value("Corner", "Full", "3D Box").visible(() -> playerSetting.isSelected("Box"));
    public BooleanSetting flatBoxOutline = new BooleanSetting("Outline", "Outline for flat boxes")
            .visible(() -> playerSetting.isSelected("Box") && !boxType.isSelected("3D Box"));
    public ValueSetting boxAlpha = new ValueSetting("Alpha", "Box transparency")
            .setValue(1.0F).range(0.1F, 1.0F).visible(() -> boxType.isSelected("3D Box"));

    public EntityESP() {
        super("EntityESP", "Entity ESP", ModuleCategory.RENDER);
        setup(sizeSetting, entityType, playerSetting, boxType, flatBoxOutline, boxAlpha);
        encMap = Map.ofEntries(
                Map.entry(Enchantments.BLAST_PROTECTION, "B"),
                Map.entry(Enchantments.PROTECTION, "P"),
                Map.entry(Enchantments.SHARPNESS, "S"),
                Map.entry(Enchantments.EFFICIENCY, "E"),
                Map.entry(Enchantments.UNBREAKING, "U"),
                Map.entry(Enchantments.POWER, "P"),
                Map.entry(Enchantments.THORNS, "T"),
                Map.entry(Enchantments.MENDING, "M"),
                Map.entry(Enchantments.DEPTH_STRIDER, "D"),
                Map.entry(Enchantments.QUICK_CHARGE, "Q"),
                Map.entry(Enchantments.MULTISHOT, "MS"),
                Map.entry(Enchantments.PIERCING, "P")
        );
    }

    public static EntityESP getInstance() {
        return Instance.get(EntityESP.class);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        players.clear();
        enchantCacheValid = false;
        enchantCache.clear();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        players.clear();
        if (mc.world == null) return;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p != mc.player) players.add(p);
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (!entityType.isSelected("Player") || !boxType.isSelected("3D Box")) return;

        float tickDelta = mc.getRenderTickCounter().getTickDelta(false);
        Vec3d cam = mc.getEntityRenderDispatcher().camera.getPos();
        int alpha = (int) (boxAlpha.getValue() * 255);

        for (int i = 0, s = players.size(); i < s; i++) {
            PlayerEntity player = players.get(i);
            if (player == null) continue;

            double interpX = MathHelper.lerp(tickDelta, player.prevX, player.getX());
            double interpY = MathHelper.lerp(tickDelta, player.prevY, player.getY());
            double interpZ = MathHelper.lerp(tickDelta, player.prevZ, player.getZ());

            if (cam.squaredDistanceTo(interpX, interpY, interpZ) < 1) continue;

            int baseColor = FriendUtils.isFriend(player) ? ColorUtil.getFriendColor() : ColorUtil.getClientColor();
            int fillColor = (baseColor & 0x00FFFFFF) | (alpha << 24);
            int outlineColor = baseColor | 0xFF000000;

            Box box = player.getDimensions(player.getPose()).getBoxAt(interpX, interpY, interpZ);
            Render3DUtil.drawBox(box, fillColor, 2, true, true, true);
            Render3DUtil.drawBox(box, outlineColor, 2, true, true, true);
        }
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        DrawContext ctx = e.getDrawContext();
        MatrixStack matrix = ctx.getMatrices();
        int fontSize = sizeSetting.getInt();
        FontRenderer font = Fonts.getSize(fontSize, Fonts.Type.DEFAULT);
        FontRenderer bigFont = Fonts.getSize(fontSize + 2, Fonts.Type.DEFAULT);
        Vec3d cam = mc.getEntityRenderDispatcher().camera.getPos();

        boolean showPlayers = entityType.isSelected("Player");
        boolean showItems = entityType.isSelected("Item");
        boolean showTnt = entityType.isSelected("TNT");

        boolean showBox = showPlayers && playerSetting.isSelected("Box");
        boolean showArmor = showPlayers && playerSetting.isSelected("Armor");
        boolean showEnchants = showPlayers && playerSetting.isSelected("Enchants");
        boolean showHands = showPlayers && playerSetting.isSelected("Hand Items");
        boolean showNameTags = showPlayers && playerSetting.isSelected("NameTags");
        boolean isReallyWorld = ServerUtil.isReallyWorld();

        if (showPlayers) {
            for (int i = 0, s = players.size(); i < s; i++) {
                PlayerEntity player = players.get(i);
                if (player == null) continue;

                Vector4d vec = ProjectionUtil.getVector4D(player);
                if (ProjectionUtil.cantSee(vec)) continue;
                if (cam.squaredDistanceTo(player.getBoundingBox().getCenter()) < 1) continue;

                boolean friend = FriendUtils.isFriend(player);

                if (showBox) drawBox(friend, vec);
                if (showArmor) drawArmor(ctx, player, vec, font, showEnchants);
                if (showHands) drawHands(matrix, player, font, vec);

                drawPlayerName(ctx, matrix, player, vec, font, friend, showNameTags, isReallyWorld);
            }
        }

        if (showItems || showTnt) {
            for (Entity entity : PlayerIntersectionUtil.streamEntities().toList()) {
                if (entity instanceof ItemEntity item && showItems) {
                    drawItemEntity(ctx, matrix, item, font, bigFont);
                } else if (entity instanceof TntEntity tnt && showTnt) {
                    Vector4d vec = ProjectionUtil.getVector4D(entity);
                    if (!ProjectionUtil.cantSee(vec)) {
                        drawText(matrix, tnt.getStyledDisplayName(), ProjectionUtil.centerX(vec), vec.y, font);
                    }
                }
            }
        }
    }

    private void drawPlayerName(DrawContext ctx, MatrixStack matrix, PlayerEntity player, Vector4d vec,
                                FontRenderer font, boolean friend, boolean nameTags, boolean reallyWorld) {
        MutableText text = buildPlayerText(player, friend, nameTags);
        double centerX = ProjectionUtil.centerX(vec);

        if (reallyWorld) {
            float width = mc.textRenderer.getWidth(text);
            float height = mc.textRenderer.fontHeight;
            float posX = (float) centerX - width / 2f;
            float posY = (float) vec.y - 11F;

            rectangle.render(ShapeProperties.create(matrix, posX - 2f, posY - 0.75f, width + 4f, height + 1.5f)
                    .round(height / 4f).color(ColorUtil.HALF_BLACK).build());
            ctx.drawText(mc.textRenderer, text, (int) posX, (int) posY + 1, ColorUtil.getColor(255), false);
        } else {
            drawText(matrix, text, centerX, vec.y - 2, font);
        }
    }

    private void drawItemEntity(DrawContext ctx, MatrixStack matrix, ItemEntity item, FontRenderer font, FontRenderer bigFont) {
        Vector4d vec = ProjectionUtil.getVector4D(item);
        if (ProjectionUtil.cantSee(vec)) return;

        ItemStack stack = item.getStack();
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);

        if (container != null) {
            List<ItemStack> list = container.stream().toList();
            if (!list.isEmpty()) {
                drawShulkerBox(ctx, stack, list, vec);
                return;
            }
        }

        Text text = stack.getName();
        boolean isEmpty = text.getContent().toString().equals("empty");

        if (stack.getCount() > 1) {
            text = ((MutableText) text).append(Formatting.RESET + " [" + Formatting.RED + stack.getCount() + Formatting.GRAY + "x" + Formatting.RESET + "]");
        }

        drawText(matrix, text, ProjectionUtil.centerX(vec), vec.y, isEmpty ? bigFont : font);
    }

    private void drawBox(boolean friend, Vector4d vec) {
        if (boxType.isSelected("3D Box")) return;

        int client = friend ? ColorUtil.getFriendColor() : ColorUtil.getClientColor();
        float posX = (float) vec.x, posY = (float) vec.y;
        float endPosX = (float) vec.z, endPosY = (float) vec.w;
        float size = (endPosX - posX) / 3;
        boolean outline = flatBoxOutline.isValue();

        if (boxType.isSelected("Corner")) {
            drawCornerBox(posX, posY, endPosX, endPosY, size, client, outline);
        } else if (boxType.isSelected("Full")) {
            drawFullBox(posX, posY, endPosX, endPosY, client, outline);
        }
    }

    private void drawCornerBox(float posX, float posY, float endPosX, float endPosY, float size, int client, boolean outline) {
        int black = ColorUtil.HALF_BLACK;

        if (outline) {
            Render2DUtil.drawQuad(posX - 1F, posY - 1, size + 1, 1.5F, black);
            Render2DUtil.drawQuad(posX - 1F, posY + 0.5F, 1.5F, size + 0.5F, black);
            Render2DUtil.drawQuad(posX - 1F, endPosY - size - 1, 1.5F, size, black);
            Render2DUtil.drawQuad(posX - 1F, endPosY - 1, size + 1, 1.5F, black);
            Render2DUtil.drawQuad(endPosX - size + 0.5F, posY - 1, size + 1, 1.5F, black);
            Render2DUtil.drawQuad(endPosX, posY + 0.5F, 1.5F, size + 0.5F, black);
            Render2DUtil.drawQuad(endPosX, endPosY - size - 1, 1.5F, size, black);
            Render2DUtil.drawQuad(endPosX - size + 0.5F, endPosY - 1, size + 1, 1.5F, black);
        }

        Render2DUtil.drawQuad(posX - 0.5F, posY - 0.5F, size, 0.5F, client);
        Render2DUtil.drawQuad(posX - 0.5F, posY, 0.5F, size + 0.5F, client);
        Render2DUtil.drawQuad(posX - 0.5F, endPosY - size - 0.5F, 0.5F, size, client);
        Render2DUtil.drawQuad(posX - 0.5F, endPosY - 0.5F, size, 0.5F, client);
        Render2DUtil.drawQuad(endPosX - size + 1, posY - 0.5F, size, 0.5F, client);
        Render2DUtil.drawQuad(endPosX + 0.5F, posY, 0.5F, size + 0.5F, client);
        Render2DUtil.drawQuad(endPosX + 0.5F, endPosY - size - 0.5F, 0.5F, size, client);
        Render2DUtil.drawQuad(endPosX - size + 1, endPosY - 0.5F, size, 0.5F, client);
    }

    private void drawFullBox(float posX, float posY, float endPosX, float endPosY, int client, boolean outline) {
        float w = endPosX - posX, h = endPosY - posY;

        if (outline) {
            int black = ColorUtil.HALF_BLACK;
            Render2DUtil.drawQuad(posX - 1F, posY - 1F, w + 2F, 1.5F, black);
            Render2DUtil.drawQuad(posX - 1F, posY - 1F, 1.5F, h + 2F, black);
            Render2DUtil.drawQuad(posX - 1F, endPosY - 1F, w + 2F, 1.5F, black);
            Render2DUtil.drawQuad(endPosX - 0.5F, posY - 1F, 1.5F, h + 2F, black);
        }

        Render2DUtil.drawQuad(posX - 0.5F, posY - 0.5F, w + 1F, 0.5F, client);
        Render2DUtil.drawQuad(posX - 0.5F, posY - 0.5F, 0.5F, h + 1F, client);
        Render2DUtil.drawQuad(posX - 0.5F, endPosY - 0.5F, w + 1F, 0.5F, client);
        Render2DUtil.drawQuad(endPosX, posY - 0.5F, 0.5F, h + 1F, client);
    }

    private void drawArmor(DrawContext ctx, PlayerEntity player, Vector4d vec, FontRenderer font, boolean showEnchants) {
        armorCache.clear();
        for (ItemStack s : player.getEquippedItems()) {
            if (!s.isEmpty()) armorCache.add(s);
        }
        if (armorCache.isEmpty()) return;

        MatrixStack matrix = ctx.getMatrices();
        int count = armorCache.size();
        float posX = (float) (ProjectionUtil.centerX(vec) - count * 5.5);
        float posY = (float) (vec.y - sizeSetting.getInt() / 1.5 - 15);

        matrix.push();
        matrix.translate(posX, posY, 0);

        rectangle.render(ShapeProperties.create(matrix, -0.5F, -0.5F, count * 11 - 1 + 1F, 11F)
                .round(2.5F).color(ColorUtil.HALF_BLACK).build());

        float fontHeight = font.getFont().getSize() / 1.5F;
        float offset = -11;

        for (int i = 0; i < count; i++) {
            ItemStack stack = armorCache.get(i);
            offset += 11;
            drawArmorItem(ctx, stack, offset);

            if (showEnchants) {
                drawEnchants(matrix, stack, font, offset, fontHeight);
            }
        }
        matrix.pop();
    }

    private void drawArmorItem(DrawContext ctx, ItemStack stack, float x) {
        MatrixStack matrix = ctx.getMatrices();
        matrix.push();
        matrix.translate(x + 1, 1, 0);
        matrix.scale(0.5F, 0.5F, 1);
        ctx.drawItem(stack, 0, 0);
        matrix.pop();
    }

    private void drawEnchants(MatrixStack matrix, ItemStack stack, FontRenderer font, float offset, float fontHeight) {
        ensureEnchantCache();
        ItemEnchantmentsComponent enchants = EnchantmentHelper.getEnchantments(stack);
        float enchantY = -fontHeight - 2;

        for (var entry : encMap.entrySet()) {
            RegistryEntry<Enchantment> regEntry = enchantCache.get(entry.getKey());
            if (regEntry != null && enchants.getEnchantments().contains(regEntry)) {
                int level = enchants.getLevel(regEntry);
                MutableText enchantText = Text.literal(entry.getValue() + level);
                float textWidth = font.getStringWidth(enchantText);
                drawText(matrix, enchantText, offset + 9f - textWidth / 2, enchantY + 8, font);
                enchantY -= fontHeight + 1;
            }
        }
    }

    private void ensureEnchantCache() {
        if (enchantCacheValid || mc.world == null) return;

        var registry = mc.world.getRegistryManager().getOptional(RegistryKeys.ENCHANTMENT);
        if (registry.isEmpty()) return;

        for (RegistryKey<Enchantment> key : encMap.keySet()) {
            registry.get().getEntry(key.getValue()).ifPresent(e -> enchantCache.put(key, e));
        }
        enchantCacheValid = true;
    }

    private void drawHands(MatrixStack matrix, PlayerEntity player, FontRenderer font, Vector4d vec) {
        double posY = vec.w;
        for (ItemStack stack : player.getHandItems()) {
            if (stack.isEmpty()) continue;

            MutableText text = Text.empty().append(stack.getName());
            if (stack.getCount() > 1) {
                text.append(Formatting.RESET + " [" + Formatting.RED + stack.getCount() + Formatting.GRAY + "x" + Formatting.RESET + "]");
            }
            posY += font.getStringHeight(text) / 2 + 3;
            drawText(matrix, text, ProjectionUtil.centerX(vec), posY, font);
        }
    }

    private void drawShulkerBox(DrawContext ctx, ItemStack itemStack, List<ItemStack> stacks, Vector4d vec) {
        MatrixStack matrix = ctx.getMatrices();
        int color = ColorUtil.multBright(ColorUtil.replAlpha(
                ((BlockItem) itemStack.getItem()).getBlock().getDefaultMapColor().color, 1F), 1);

        matrix.push();
        matrix.translate(ProjectionUtil.centerX(vec) - SHULKER_WIDTH / 4.0, vec.w + 2, -200 + Math.cos(vec.x));
        matrix.scale(0.5F, 0.5F, 1);

        ctx.drawTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, 0, 0, SHULKER_WIDTH, SHULKER_HEIGHT, SHULKER_WIDTH, SHULKER_HEIGHT, color);

        int posX = 7, posY = 6;
        for (ItemStack stack : stacks) {
            drawShulkerItem(ctx, stack, posX, posY);
            posX += 18;
            if (posX >= 165) {
                posY += 18;
                posX = 7;
            }
        }
        matrix.pop();
    }

    private void drawShulkerItem(DrawContext ctx, ItemStack stack, int x, int y) {
        ctx.drawItem(stack, x, y);
        ctx.drawStackOverlay(mc.textRenderer, stack, x, y);
    }

    private void drawText(MatrixStack matrix, Text text, double startX, double startY, FontRenderer font) {
        float height = font.getFont().getSize() / 1.5F;
        float width = font.getStringWidth(text);
        float posX = (float) startX - width / 2;
        float posY = (float) startY - height;

        rectangle.render(ShapeProperties.create(matrix, posX - 2, posY - 0.75F, width + 4, height + 1.5F)
                .round(height / 4).color(ColorUtil.HALF_BLACK).build());
        font.drawText(matrix, text, posX, posY + 3);
    }

    private MutableText buildPlayerText(PlayerEntity player, boolean friend, boolean nameTags) {
        MutableText text = Text.empty();

        if (friend) text.append("[" + Formatting.GREEN + "F" + Formatting.RESET + "] ");
        if (AntiBot.getInstance().isBot(player)) text.append("[" + Formatting.DARK_RED + "BOT" + Formatting.RESET + "] ");

        text.append(nameTags ? player.getDisplayName() : player.getName());

        ItemStack offHand = player.getOffHandStack();
        if (offHand.isOf(Items.PLAYER_HEAD) || offHand.isOf(Items.TOTEM_OF_UNDYING)) {
            text.append(Formatting.RESET + getSphere(offHand));
        }

        float health = PlayerIntersectionUtil.getHealth(player);
        if (health >= 0 && health <= player.getMaxHealth()) {
            text.append(Formatting.RESET + " [" + Formatting.RED + PlayerIntersectionUtil.getHealthString(player) + Formatting.RESET + "]");
        }

        return text;
    }

    private String getSphere(ItemStack stack) {
        if (!ServerUtil.isFunTime()) return "";

        var component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if (component == null) return "";

        NbtCompound compound = component.copyNbt();
        if (compound.getInt("tslevel") != 0) {
            return " [" + Formatting.GOLD + compound.getString("don-item").replace("sphere-", "").toUpperCase() + Formatting.RESET + "]";
        }
        return "";
    }
}
