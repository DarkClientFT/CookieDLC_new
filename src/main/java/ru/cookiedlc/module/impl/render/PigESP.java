package ru.cookiedlc.module.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.PigEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PigEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.common.util.math.MathUtil;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.event.events.render.WorldRenderEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.module.impl.combat.KillAura;

public class PigESP extends Module {

    private final SelectSetting mode = new SelectSetting("Вид", "Кого рендерить")
            .value("Свэня").selected("Свэня");

    private final ValueSetting speed = new ValueSetting("Скрст", "speed")
            .setValue(1.5F).range(0.5f, 3.0f);

    private LivingEntity target;
    private boolean rendering = false;
    private PigEntity cachedPig;
    private PigEntityRenderer pigRenderer;
    private LivingEntityRenderState cachedState;

    public PigESP() {
        super("PigESP", "PigESP", ModuleCategory.MISC);
        setup(mode, speed);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        rendering = false;
        target = null;
        cachedPig = null;
        pigRenderer = null;
        cachedState = null;
    }

    @EventHandler
    private void onTick(TickEvent event) {
        KillAura aura = KillAura.getInstance();
        if (aura != null && aura.isState()) {
            LivingEntity auraTarget = aura.getTarget();
            if (auraTarget != null && auraTarget != mc.player) {
                target = auraTarget;
                rendering = true;
                return;
            }
        }
        rendering = false;
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (!rendering || target == null || mc.world == null) return;
        if (!mode.isSelected("Свэня")) return;

        float partialTicks = e.getPartialTicks();

        double interpX = MathUtil.interpolate(target.prevX, target.getX());
        double interpY = MathUtil.interpolate(target.prevY, target.getY());
        double interpZ = MathUtil.interpolate(target.prevZ, target.getZ());

        Camera camera = mc.getEntityRenderDispatcher().camera;
        Vec3d camPos = camera.getPos();

        double x = interpX - camPos.x;
        double y = interpY - camPos.y;
        double z = interpZ - camPos.z;

        float radius = 0.7f;
        float orbitHeight = 1.0f;
        float speedMul = 0.00025f * speed.getValue();
        float time = -(System.currentTimeMillis() % 1000000) * speedMul;

        double[] px = new double[8];
        double[] py = new double[8];
        double[] pz = new double[8];

        float baseAngle = time * 360;
        for (int i = 0; i < 8; i++) {
            float angle = baseAngle + (i / 8.0f) * 360f;
            double rad = Math.toRadians(angle);
            float verticalOffset = (i % 2 == 0) ? 0.1f : -0.1f;
            px[i] = x + Math.cos(rad) * radius;
            py[i] = y + orbitHeight + verticalOffset - 0.2f;
            pz[i] = z + Math.sin(rad) * radius;
        }

        double topPigX = x;
        double topPigY = y + 2.2f;
        double topPigZ = z;

        float timeAlt = (System.currentTimeMillis() % 1000000) * speed.getValue() * 0.001f;
        float topYaw = timeAlt * 180;
        float topPitch = (float) (Math.sin(timeAlt * 1.5) * 120);
        float topRoll = (float) (Math.cos(timeAlt * 1.2) * 90);

        if (cachedPig == null || !cachedPig.isAlive()) {
            cachedPig = new PigEntity(EntityType.PIG, mc.world);
        }

        if (pigRenderer == null) {
            EntityRenderer<? super PigEntity, ?> renderer = mc.getEntityRenderDispatcher().getRenderer(cachedPig);
            if (renderer instanceof PigEntityRenderer pr) {
                pigRenderer = pr;
                cachedState = pr.createRenderState();
            } else {
                return;
            }
        }

        if (cachedState == null) return;

        cachedPig.limbAnimator.setSpeed(0);
        cachedPig.age = 0;
        cachedPig.bodyYaw = 0;
        cachedPig.prevBodyYaw = 0;
        cachedPig.headYaw = 0;
        cachedPig.prevHeadYaw = 0;
        cachedPig.setPitch(0);
        cachedPig.prevPitch = 0;

        pigRenderer.updateRenderState(cachedPig, (PigEntityRenderState) cachedState, partialTicks);

        VertexConsumerProvider.Immediate immediate = mc.getBufferBuilders().getEntityVertexConsumers();

        RenderSystem.disableDepthTest();

        for (int i = 0; i < 9; i++) {
            double posX, posY, posZ;
            double nextX, nextY, nextZ;

            if (i < 8) {
                posX = px[i];
                posY = py[i];
                posZ = pz[i];
                int nextIndex = (i + 1) % 8;
                nextX = px[nextIndex];
                nextY = py[nextIndex];
                nextZ = pz[nextIndex];
            } else {
                posX = topPigX;
                posY = topPigY;
                posZ = topPigZ;
                nextX = px[0];
                nextY = py[0];
                nextZ = pz[0];
            }

            MatrixStack stack = new MatrixStack();
            stack.push();

            stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            stack.translate(posX, posY, posZ);

            if (i == 8) {
                stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(topYaw));
                stack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(topPitch));
                stack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(topRoll));
            } else {
                double lX = nextX - posX;
                double lZ = nextZ - posZ;
                float yaw = (float) Math.toDegrees(Math.atan2(-lZ, lX)) - 95;
                stack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw));
            }

            float scale = (i == 8) ? 0.4f : 0.3f;
            stack.scale(scale, scale, scale);

            try {
                pigRenderer.render((PigEntityRenderState) cachedState, stack, immediate, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            } catch (Exception ignored) {
            }

            stack.pop();
        }

        immediate.draw();
        RenderSystem.enableDepthTest();
    }
}