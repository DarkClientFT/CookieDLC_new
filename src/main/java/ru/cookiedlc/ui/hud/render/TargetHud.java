package ru.cookiedlc.ui.hud.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import ru.cookiedlc.module.impl.combat.TriggerBot;
import ru.cookiedlc.ui.hud.api.AbstractDraggable;
import ru.cookiedlc.api.system.font.FontRenderer;
import ru.cookiedlc.api.system.font.Fonts;
import ru.cookiedlc.api.system.shape.ShapeProperties;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.render.Render2DUtil;
import ru.cookiedlc.common.util.render.ScissorManager;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.module.impl.combat.KillAura;

import static ru.cookiedlc.api.system.font.Fonts.Type.BOLD;
import static ru.cookiedlc.api.system.font.Fonts.Type.ICO3;

public class TargetHud extends AbstractDraggable {

    private LivingEntity currentTarget;
    private float health;
    private float absHealth;

    private static final float H = 14f;

    public TargetHud() {
        super("Target Hud", 10, 10, 95, 30, true);
    }

    @Override
    public boolean visible() {
        return currentTarget != null;
    }

    @Override
    public void tick() {
        LivingEntity auraTarget = KillAura.getInstance().getTarget();
        if (mc.currentScreen instanceof ChatScreen) {
            currentTarget = mc.player;
            startAnimation();
        } else if (auraTarget != null) {
            currentTarget = auraTarget;
            startAnimation();
        } else {
            currentTarget = null;
            stopAnimation();
        }
    }

    @Override
    public void drawDraggable(DrawContext context) {
        if (currentTarget == null) return;

        MatrixStack ms = context.getMatrices();

        FontRenderer bold   = Fonts.getSize(13, BOLD);
        FontRenderer boldLg = Fonts.getSize(15, BOLD);
        FontRenderer ico    = Fonts.getSize(14, ICO3);

        int theme    = ColorUtil.getClientColor();
        int themeDim = darken(theme, 0.3f);
        int txtColor = 0xFFDDDAEB;
        int sepColor = 0x40FFFFFF;
        int bgColor  = 0xCC111118;
        int bdColor  = 0xFF23232D;

        float posX = getX();
        float posY = getY();

        float hp         = currentTarget.getHealth();
        float maxHp      = currentTarget.getMaxHealth();
        float absorption = currentTarget.getAbsorptionAmount();
        float hpRatio    = MathHelper.clamp(hp / maxHp, 0f, 1f);

        String nameText = currentTarget.getName().getString();
        String hpText   = getHealth(20) + " HP";

        float slotSize = 10f;
        float slotStep = 12f;
        float gap      = 2f;

        ItemStack slotHead  = currentTarget.getEquippedStack(EquipmentSlot.HEAD);
        ItemStack slotChest = currentTarget.getEquippedStack(EquipmentSlot.CHEST);
        ItemStack slotLegs  = currentTarget.getEquippedStack(EquipmentSlot.LEGS);
        ItemStack slotFeet  = currentTarget.getEquippedStack(EquipmentSlot.FEET);

        float armorBlockW = 4 * slotStep + 4f;

        float row1Y = posY;
        float row2Y = posY + H + gap;

        float avatarW = 26f;

        float curX = posX + avatarW + gap;

        float nameW = boldLg.getStringWidth(nameText) + 10f;
        drawRect(ms, curX, row1Y, nameW, H, bgColor, bdColor);
        boldLg.drawGradientString(ms, nameText, curX + 5f, row1Y + 4.5f, theme, themeDim);

        float armorX = curX + nameW + 5f;
        drawRect(ms, armorX, row1Y, armorBlockW, H, bgColor, bdColor);

        float slotY = row1Y + (H - slotSize) / 2f;
        drawSlot(context, ms, armorX + 2f,                    slotY, slotHead,  slotSize);
        drawSlot(context, ms, armorX + 2f + slotStep,         slotY, slotChest, slotSize);
        drawSlot(context, ms, armorX + 2f + slotStep * 2f,    slotY, slotLegs,  slotSize);
        drawSlot(context, ms, armorX + 2f + slotStep * 3f,    slotY, slotFeet,  slotSize);

        float totalW  = (armorX + armorBlockW) - posX;
        drawRect(ms, curX, row2Y, totalW - avatarW - gap, H, bgColor, bdColor);

        String icoHp  = "F";
        float icoHpW  = ico.getStringWidth(icoHp);
        float hpTextW = bold.getStringWidth(hpText);

        float bx = curX + 5f;
        ico.drawString(ms, icoHp, bx, row2Y + 6f, theme);
        bx += icoHpW + 3f;
        drawSep(ms, bx, row2Y, H, sepColor);
        bx += 5f;
        bold.drawString(ms, hpText, bx, row2Y + 5.5f, txtColor);
        bx += hpTextW + 4f;

        float barW = (curX + totalW - avatarW - gap) - bx - 5f;
        health    = MathHelper.clamp(MathHelper.lerp(0.15F, health, hpRatio * barW), 0, barW);
        absHealth = MathHelper.clamp(MathHelper.lerp(0.15F, absHealth,
                MathHelper.clamp(absorption / maxHp, 0f, 1f) * barW), 0, barW);

        int healthColor;
        if (hpRatio <= 0.3f)      healthColor = 0xFFFF4B4B;
        else if (hpRatio <= 0.7f) healthColor = 0xFFFFB84B;
        else                      healthColor = 0xFF4BFF65;

        float barY = row2Y + (H - 2.5f) / 2f;
        renderShape(ms, bx, barY, barW, 2.5f, ShapeProperties.create(ms, bx, barY, barW, 2.5f)
                .round(2).color(0xFF060712).build());
        renderShape(ms, bx, barY, health, 2.5f, ShapeProperties.create(ms, bx, barY, health, 2.5f)
                .round(2).color(healthColor).build());
        if (absorption > 0f) {
            renderShape(ms, bx, barY, absHealth, 2.5f, ShapeProperties.create(ms, bx, barY, absHealth, 2.5f)
                    .round(2).color(0xFFFFD700).build());
        }

        setWidth((int) totalW);
        setHeight((int) (H * 2 + gap));
    }

    private void drawSlot(DrawContext context, MatrixStack ms,
                          float x, float y, ItemStack stack,
                          float slotSize) {
        if (!stack.isEmpty()) {
            Render2DUtil.defaultDrawStack(context, stack, x, y + 0.5f, false, false, 0.5f);
        } else {
            FontRenderer emptyIco = Fonts.getSize(11, Fonts.Type.ICO);
            String emptyChar = "";
            float ew = emptyIco.getStringWidth(emptyChar);
            float eh = emptyIco.getStringHeight(emptyChar);
            emptyIco.drawString(ms, emptyChar,
                    x + (slotSize - ew) / 2f,
                    y + (slotSize - eh) / 2f + 3f,
                    0x60FFFFFF);
        }
    }


    private void drawFace(DrawContext context, float x, float y) {
        EntityRenderer<? super LivingEntity, ?> baseRenderer = mc.getEntityRenderDispatcher().getRenderer(currentTarget);
        if (!(baseRenderer instanceof LivingEntityRenderer<?, ?, ?>)) return;

        @SuppressWarnings("unchecked")
        LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?> renderer =
                (LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>) baseRenderer;
        LivingEntityRenderState state = renderer.getAndUpdateRenderState(currentTarget, tickCounter.getTickDelta(false));
        Identifier textureLocation = renderer.getTexture(state);

        Render2DUtil.drawTexture(context, textureLocation, x, y, 21, 3, 8, 8, 64,
                ColorUtil.getRect(1), ColorUtil.multRed(-1, 1 + currentTarget.hurtTime / 4F));
    }

    private void drawRect(MatrixStack ms, float x, float y, float w, float h, int bg, int bd) {
        renderShape(ms, x, y, w, h, ShapeProperties.create(ms, x, y, w, h)
                .round(3)
                .softness(1)
                .thickness(1)
                .outlineColor(bd)
                .color(bg)
                .build());
    }

    private void drawSep(MatrixStack ms, float x, float y, float h, int color) {
        rectangle.render(ShapeProperties.create(ms, x, y + (h - 8f) / 2f, 0.5f, 8f)
                .color(color)
                .build());
    }

    private int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int)(((color >> 16) & 0xFF) * (1f - factor));
        int g = (int)(((color >>  8) & 0xFF) * (1f - factor));
        int b = (int)((color & 0xFF)          * (1f - factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private int getHealth(int cent) {
        return Math.round((currentTarget.getHealth() / currentTarget.getMaxHealth()) * cent);
    }

    private int getAbsorption(int cent) {
        float absorption = currentTarget.getAbsorptionAmount();
        float maxHp      = currentTarget.getMaxHealth();
        return Math.round(MathHelper.clamp(absorption / maxHp, 0f, 1f) * cent);
    }
}