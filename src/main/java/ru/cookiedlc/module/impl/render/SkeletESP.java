package ru.cookiedlc.module.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.api.repository.friend.FriendUtils;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.common.util.render.Render3DUtil;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.render.WorldRenderEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;


import java.util.HashMap;
import java.util.Map;

import static ru.cookiedlc.common.QuickImports.mc;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class SkeletESP extends Module {
    public static SkeletESP getInstance() {
        return Instance.get(SkeletESP.class);
    }

    final BooleanSetting players = new BooleanSetting("Игроки", "Показывать скелет игроков").setValue(true);
    final BooleanSetting friends = new BooleanSetting("Друзья", "Показывать скелет друзей").setValue(true);
    final ValueSetting lineWidth = new ValueSetting("Толщина линий", "Толщина линий скелета")
            .setValue(2.0f).range(0.5f, 5.0f);

    private static final float DISTANCE = 128.0f;

    public SkeletESP() {
        super("SkeletonESP", "Skeleton ESP", ModuleCategory.RENDER);
        setup(players, friends, lineWidth);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (mc.world == null || mc.player == null) return;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!shouldRender(player)) continue;

            float dist = mc.player.distanceTo(player);
            if (dist > DISTANCE) continue;

            renderSkeleton(player, e.getPartialTicks());
        }
    }

    private boolean shouldRender(PlayerEntity player) {
        if (player == null || !player.isAlive() || player.age < 3) return false;
        if (player == mc.player) return false;

        boolean isFriend = FriendUtils.isFriend(player.getName().getString());
        if (isFriend && !friends.isValue()) return false;
        if (!isFriend && !players.isValue()) return false;

        return true;
    }

    private void renderSkeleton(PlayerEntity player, float partialTicks) {
        Vec3d pos = MathUtil.interpolate(player);
        int color = getColor(player);
        float width = lineWidth.getValue();

        float limbSwing = player.limbAnimator.getPos(partialTicks);
        float limbSwingAmount = player.limbAnimator.getSpeed(partialTicks);

        float bodyYaw = MathHelper.lerpAngleDegrees(partialTicks, player.prevBodyYaw, player.bodyYaw);
        float bodyYawRad = (float) Math.toRadians(-bodyYaw + 90);

        float sneakOffset = player.isSneaking() ? 0.2f : 0f;
        Vec3d head = pos.add(0, 1.62f - sneakOffset, 0);
        Vec3d neck = pos.add(0, 1.4f - sneakOffset, 0);
        Vec3d body = pos.add(0, 0.9f - sneakOffset, 0);
        Vec3d pelvis = pos.add(0, 0.6f - sneakOffset, 0);

        Render3DUtil.drawLine(head, neck, color, width, false);
        Render3DUtil.drawLine(neck, body, color, width, false);
        Render3DUtil.drawLine(body, pelvis, color, width, false);

        float rightArmSwing = MathHelper.cos(limbSwing * 0.6662f) * limbSwingAmount;
        float leftArmSwing = MathHelper.cos(limbSwing * 0.6662f + (float)Math.PI) * limbSwingAmount;
        float rightLegSwing = MathHelper.cos(limbSwing * 0.6662f + (float)Math.PI) * 1.4f * limbSwingAmount;
        float leftLegSwing = MathHelper.cos(limbSwing * 0.6662f) * 1.4f * limbSwingAmount;

        Vec3d rightShoulder = neck.add(
                Math.sin(bodyYawRad) * 0.3,
                -0.1,
                Math.cos(bodyYawRad) * 0.3
        );

        Vec3d rightElbow = rightShoulder.add(
                Math.sin(bodyYawRad) * 0.1 + Math.sin(bodyYawRad + Math.PI/2) * rightArmSwing * 0.3,
                -0.3 - Math.abs(rightArmSwing) * 0.15,
                Math.cos(bodyYawRad) * 0.1 + Math.cos(bodyYawRad + Math.PI/2) * rightArmSwing * 0.3
        );

        Vec3d rightHand = rightElbow.add(
                Math.sin(bodyYawRad + Math.PI/2) * rightArmSwing * 0.2,
                -0.3 - Math.abs(rightArmSwing) * 0.1,
                Math.cos(bodyYawRad + Math.PI/2) * rightArmSwing * 0.2
        );

        Render3DUtil.drawLine(rightShoulder, rightElbow, color, width, false);
        Render3DUtil.drawLine(rightElbow, rightHand, color, width, false);

        Vec3d leftShoulder = neck.add(
                -Math.sin(bodyYawRad) * 0.3,
                -0.1,
                -Math.cos(bodyYawRad) * 0.3
        );

        Vec3d leftElbow = leftShoulder.add(
                -Math.sin(bodyYawRad) * 0.1 + Math.sin(bodyYawRad + Math.PI/2) * leftArmSwing * 0.3,
                -0.3 - Math.abs(leftArmSwing) * 0.15,
                -Math.cos(bodyYawRad) * 0.1 + Math.cos(bodyYawRad + Math.PI/2) * leftArmSwing * 0.3
        );

        Vec3d leftHand = leftElbow.add(
                Math.sin(bodyYawRad + Math.PI/2) * leftArmSwing * 0.2,
                -0.3 - Math.abs(leftArmSwing) * 0.1,
                Math.cos(bodyYawRad + Math.PI/2) * leftArmSwing * 0.2
        );

        Render3DUtil.drawLine(leftShoulder, leftElbow, color, width, false);
        Render3DUtil.drawLine(leftElbow, leftHand, color, width, false);

        Vec3d rightHip = pelvis.add(
                Math.sin(bodyYawRad) * 0.15,
                0,
                Math.cos(bodyYawRad) * 0.15
        );

        Vec3d rightKnee = rightHip.add(
                Math.sin(bodyYawRad + Math.PI/2) * rightLegSwing * 0.2,
                -0.4 + Math.max(0, rightLegSwing) * 0.1,
                Math.cos(bodyYawRad + Math.PI/2) * rightLegSwing * 0.2
        );

        Vec3d rightFoot = rightKnee.add(
                Math.sin(bodyYawRad + Math.PI/2) * rightLegSwing * 0.15,
                -0.4 - Math.max(0, -rightLegSwing) * 0.1,
                Math.cos(bodyYawRad + Math.PI/2) * rightLegSwing * 0.15
        );

        Render3DUtil.drawLine(rightHip, rightKnee, color, width, false);
        Render3DUtil.drawLine(rightKnee, rightFoot, color, width, false);

        Vec3d leftHip = pelvis.add(
                -Math.sin(bodyYawRad) * 0.15,
                0,
                -Math.cos(bodyYawRad) * 0.15
        );

        Vec3d leftKnee = leftHip.add(
                Math.sin(bodyYawRad + Math.PI/2) * leftLegSwing * 0.2,
                -0.4 + Math.max(0, leftLegSwing) * 0.1,
                Math.cos(bodyYawRad + Math.PI/2) * leftLegSwing * 0.2
        );

        Vec3d leftFoot = leftKnee.add(
                Math.sin(bodyYawRad + Math.PI/2) * leftLegSwing * 0.15,
                -0.4 - Math.max(0, -leftLegSwing) * 0.1,
                Math.cos(bodyYawRad + Math.PI/2) * leftLegSwing * 0.15
        );

        Render3DUtil.drawLine(leftHip, leftKnee, color, width, false);
        Render3DUtil.drawLine(leftKnee, leftFoot, color, width, false);

        Render3DUtil.drawLine(rightShoulder, leftShoulder, color, width, false);
        Render3DUtil.drawLine(rightHip, leftHip, color, width, false);
    }

    private int getColor(PlayerEntity player) {
        if (player == mc.player) {
            return ColorUtil.getClientColor();
        }

        boolean isFriend = FriendUtils.isFriend(player.getName().getString());
        if (isFriend) {
            return ColorUtil.getFriendColor();
        }

        float red = MathHelper.clamp((player.hurtTime - tickCounter.getTickDelta(false)) / 10, 0, 1);
        return ColorUtil.multRed(ColorUtil.getClientColor(), 1 + red * 10);
    }
}
