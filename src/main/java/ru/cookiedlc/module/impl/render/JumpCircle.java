package ru.cookiedlc.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.other.StopWatch;
import ru.cookiedlc.common.util.render.Render3DUtil;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.event.events.render.WorldRenderEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.SelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JumpCircle extends Module implements QuickImports {

    ValueSetting lifetimeMs = new ValueSetting("Lifetime", "Time to live in ms").setValue(1500F).range(500F, 3000F);
    ValueSetting radiusSetting = new ValueSetting("Radius", "Circle base radius").setValue(1.0F).range(0.5F, 1.5F);
    SelectSetting renderStyle = new SelectSetting("Render Style", "How to render the circle")
            .value("Lines", "Glow", "Lines+Glow").selected("Glow");

    @NonFinal List<Circle> circles = new ArrayList<>();
    @NonFinal boolean wasOnGround = true;
    @NonFinal float airborneFallDistance = 0.0f;

    public JumpCircle() {
        super("JumpCircle", ModuleCategory.RENDER);
        setup(lifetimeMs, radiusSetting, renderStyle);
    }

    @Override

    public void deactivate() {
        circles.clear();
        super.deactivate();
    }

    @Override

    public void activate() {
        circles.clear();
        if (mc.player != null) {
            wasOnGround = mc.player.isOnGround();
        } else {
            wasOnGround = true;
        }
        airborneFallDistance = 0.0f;
        super.activate();
    }

    @EventHandler

    public void onTick(TickEvent e) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();
        if (onGround) {
            if (!wasOnGround && airborneFallDistance > 0.0f) {
                addCircle(mc.player);
            }
            airborneFallDistance = 0.0f;
        } else {
            airborneFallDistance = Math.max(airborneFallDistance, mc.player.fallDistance);
        }
        wasOnGround = onGround;
    }

    @EventHandler

    public void onWorldRender(WorldRenderEvent e) {
        if (circles.isEmpty() || mc.player == null || mc.world == null) return;
        float lifetime = lifetimeMs.getValue();

        Iterator<Circle> it = circles.iterator();
        while (it.hasNext()) {
            Circle c = it.next();
            float elapsed = c.timer.elapsedTime();
            if (elapsed >= lifetime * 2.0f) {
                it.remove();
                continue;
            }

            Vec3d playerEyePos = mc.player.getCameraPosVec(e.getPartialTicks());
            Vec3d circlePos = c.pos.add(0, 0.1, 0);
            if (!mc.world.raycast(new net.minecraft.world.RaycastContext(playerEyePos, circlePos, net.minecraft.world.RaycastContext.ShapeType.VISUAL, net.minecraft.world.RaycastContext.FluidHandling.NONE, mc.player)).getType().equals(net.minecraft.util.hit.HitResult.Type.MISS)) {
                continue;
            }

            float t = Math.min(1.0f, elapsed / lifetime);
            float scale = t;
            float baseR = radiusSetting.getValue();
            float radius = baseR * (0.6f + 0.6f * scale);
            float alpha;
            if (elapsed < lifetime / 4) {
                alpha = Math.max(0.0f, Math.min(1.0f, 0.85f * (0.3f + 0.7f * (elapsed / (lifetime / 4)))));
            } else if (elapsed < lifetime / 2) {
                alpha = 0.85f;
            } else {
                alpha = Math.max(0.0f, Math.min(1.0f, 0.85f * (1.0f - (elapsed - lifetime / 2) / (lifetime / 2))));
            }

            switch (renderStyle.getSelected()) {
                case "Lines" -> drawRingLines(c.pos, radius, alpha, 3.22F);
                case "Glow" -> drawGlow(c.pos, radius, alpha);
                case "Lines+Glow" -> {
                    drawGlow(c.pos, radius, alpha);
                    drawRingLines(c.pos, radius, alpha, 3.22F);
                }
            }
        }
    }

    private void drawRingLines(Vec3d center, float radius, float alpha, float widthF) {
        int mul = Math.max(1, Math.round(3.0F));
        int steps = 90;
        double twoPi = Math.PI * 2.0;
        int width = Math.max(1, Math.round(widthF));
        for (int i = 0; i <= steps; i++) {
            double a1 = (i * twoPi) / steps;
            double a2 = ((i + mul) * twoPi) / steps;
            Vec3d p1 = new Vec3d(center.x + radius * Math.cos(a1), center.y, center.z + radius * Math.sin(a1));
            Vec3d p2 = new Vec3d(center.x + radius * Math.cos(a2), center.y, center.z + radius * Math.sin(a2));
            int color = ColorUtil.multAlpha(ColorUtil.fade(i * 4), alpha);
            Render3DUtil.drawLine(p1, p2, color, width, false);
        }
    }

    private void drawGlow(Vec3d center, float radius, float alpha) {
        int layers = Math.max(1, Math.round(6.0F));
        float spread = 0.05F;
        float layerAlpha = alpha / (layers + 0.5f);
        for (int l = 0; l < layers; l++) {
            float t = (l + 1) / (float) layers;
            float r = radius + t * spread;
            float a = layerAlpha * (1.0f - (t * 0.85f));
            drawRingLines(center, r, a, Math.max(1.0f, 3.22F + (layers - l)));
        }
    }

    private void addCircle(Entity entity) {
        Vec3d pos = entity.getPos();
        BlockPos bp = BlockPos.ofFloored(pos);
        if (mc.world != null && mc.world.getBlockState(bp).getBlock().toString().toLowerCase().contains("snow")) {
            pos = pos.add(0.0, 0.125, 0.0);
        }
        circles.add(new Circle(pos));
    }

    private static final class Circle {
        final StopWatch timer = new StopWatch();
        final Vec3d pos;
        Circle(Vec3d pos) { this.pos = pos; }
    }
}
