package ru.cookiedlc.module.impl.render;


import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.particle.ParticlesMode;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.GroupSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Optimizer extends Module {

    int origRenderDistance;
    int origSimulationDistance;
    boolean origVsync;
    boolean origEntityShadows;
    boolean origViewBobbing;
    double origFov;
    int origMaxFps;
    GraphicsMode origGraphics;
    CloudRenderMode origClouds;
    ParticlesMode origParticles;
    double origEntityDistance;
    boolean origSmoothLighting;
    int origGuiScale;
    int origMipmapLevels;
    double origDistortionEffects;
    double origGlintSpeed;
    double origDamageTilt;
    boolean origAutosaveIndicator;


    final GroupSetting renderGroup = new GroupSetting("Render", "Настройки рендера").setValue(true);
    final ValueSetting renderDistance = new ValueSetting("Render Distance", "Дальность прорисовки")
            .setValue(4f).range(2f, 16f);
    final ValueSetting simulationDistance = new ValueSetting("Simulation Distance", "Дальность симуляции")
            .setValue(4f).range(2f, 16f);
    final BooleanSetting fastGraphics = new BooleanSetting("Fast Graphics", "Быстрая графика")
            .setValue(true);
    final BooleanSetting noVsync = new BooleanSetting("No VSync", "Отключить VSync")
            .setValue(true);
    final ValueSetting maxFps = new ValueSetting("Max FPS", "Ограничение FPS (260 = unlimited)")
            .setValue(260f).range(30f, 260f);

    final GroupSetting effectsGroup = new GroupSetting("Effects", "Визуальные эффекты").setValue(true);
    final BooleanSetting noParticles = new BooleanSetting("No Particles", "Отключить частицы")
            .setValue(true);
    final BooleanSetting noClouds = new BooleanSetting("No Clouds", "Отключить облака")
            .setValue(true);
    final BooleanSetting noEntityShadows = new BooleanSetting("No Entity Shadows", "Отключить тени сущностей")
            .setValue(true);
    final BooleanSetting noViewBobbing = new BooleanSetting("No View Bobbing", "Отключить покачивание камеры")
            .setValue(true);
    final BooleanSetting noSmoothLighting = new BooleanSetting("No Smooth Lighting", "Отключить плавное освещение")
            .setValue(true);
    final BooleanSetting noDistortion = new BooleanSetting("No Distortion", "Убрать искажения экрана")
            .setValue(true);
    final BooleanSetting noGlint = new BooleanSetting("No Glint", "Убрать блеск зачарований")
            .setValue(true);
    final BooleanSetting noDamageTilt = new BooleanSetting("No Damage Tilt", "Убрать наклон при уроне")
            .setValue(true);

    final GroupSetting entityGroup = new GroupSetting("Entity", "Настройки сущностей").setValue(true);
    final ValueSetting entityDistance = new ValueSetting("Entity Distance", "Дальность рендера сущностей (%)")
            .setValue(50f).range(25f, 200f);

    final GroupSetting miscGroup = new GroupSetting("Misc", "Прочее").setValue(true);
    final BooleanSetting lowFov = new BooleanSetting("Low FOV", "Понизить FOV до 70")
            .setValue(false);
    final ValueSetting mipmapLevels = new ValueSetting("Mipmap Levels", "Уровни мипмаппинга")
            .setValue(0f).range(0f, 4f);
    final BooleanSetting noAutosaveIndicator = new BooleanSetting("No Autosave Indicator", "Убрать индикатор автосохранения")
            .setValue(true);
    final BooleanSetting fullscreen = new BooleanSetting("Fullscreen", "Полный экран (даёт FPS)")
            .setValue(false);

    public Optimizer() {
        super("Optimizer", ModuleCategory.RENDER);
        renderGroup.settings(renderDistance, simulationDistance, fastGraphics, noVsync, maxFps);
        effectsGroup.settings(noParticles, noClouds, noEntityShadows, noViewBobbing,
                noSmoothLighting, noDistortion, noGlint, noDamageTilt);
        entityGroup.settings(entityDistance);
        miscGroup.settings(lowFov, mipmapLevels, noAutosaveIndicator, fullscreen);
        setup(renderGroup, effectsGroup, entityGroup, miscGroup);
    }

    @Override
    public void activate() {
        super.activate();
        if (mc.options == null) return;
        saveOriginalSettings();
        applyOptimizedSettings();
    }

    @Override
    public void deactivate() {
        if (mc.options != null) {
            restoreOriginalSettings();
        }
        super.deactivate();
    }

    private void saveOriginalSettings() {
        origRenderDistance = mc.options.getViewDistance().getValue();
        origSimulationDistance = mc.options.getSimulationDistance().getValue();
        origVsync = mc.options.getEnableVsync().getValue();
        origEntityShadows = mc.options.getEntityShadows().getValue();
        origViewBobbing = mc.options.getBobView().getValue();
        origFov = mc.options.getFov().getValue();
        origMaxFps = mc.options.getMaxFps().getValue();
        origGraphics = mc.options.getGraphicsMode().getValue();
        origClouds = mc.options.getCloudRenderMode().getValue();
        origParticles = mc.options.getParticles().getValue();
        origEntityDistance = mc.options.getEntityDistanceScaling().getValue();
        origSmoothLighting = mc.options.getAo().getValue();
        origMipmapLevels = mc.options.getMipmapLevels().getValue();
        origDistortionEffects = mc.options.getDistortionEffectScale().getValue();
        origGlintSpeed = mc.options.getGlintSpeed().getValue();
        origDamageTilt = mc.options.getDamageTiltStrength().getValue();
        origAutosaveIndicator = mc.options.getShowAutosaveIndicator().getValue();
    }

    private void applyOptimizedSettings() {
        mc.options.getViewDistance().setValue(Math.round(renderDistance.getValue()));
        mc.options.getSimulationDistance().setValue(Math.round(simulationDistance.getValue()));

        if (fastGraphics.isValue()) {
            mc.options.getGraphicsMode().setValue(GraphicsMode.FAST);
        }

        if (noVsync.isValue()) {
            mc.options.getEnableVsync().setValue(false);
        }

        int fps = Math.round(maxFps.getValue());
        mc.options.getMaxFps().setValue(fps >= 260 ? 260 : fps);

        if (noParticles.isValue()) {
            mc.options.getParticles().setValue(ParticlesMode.MINIMAL);
        }

        if (noClouds.isValue()) {
            mc.options.getCloudRenderMode().setValue(CloudRenderMode.OFF);
        }

        if (noEntityShadows.isValue()) {
            mc.options.getEntityShadows().setValue(false);
        }

        if (noViewBobbing.isValue()) {
            mc.options.getBobView().setValue(false);
        }

        if (noSmoothLighting.isValue()) {
            mc.options.getAo().setValue(false);
        }

        if (noDistortion.isValue()) {
            mc.options.getDistortionEffectScale().setValue(0.0);
        }

        if (noGlint.isValue()) {
            mc.options.getGlintSpeed().setValue(0.0);
        }

        if (noDamageTilt.isValue()) {
            mc.options.getDamageTiltStrength().setValue(0.0);
        }

        mc.options.getEntityDistanceScaling().setValue((double) entityDistance.getValue() / 100.0);

        if (lowFov.isValue()) {
            mc.options.getFov().setValue(70);
        }

        mc.options.getMipmapLevels().setValue(Math.round(mipmapLevels.getValue()));

        if (noAutosaveIndicator.isValue()) {
            mc.options.getShowAutosaveIndicator().setValue(false);
        }

        if (fullscreen.isValue() && !mc.getWindow().isFullscreen()) {
            mc.getWindow().toggleFullscreen();
        }

        mc.options.write();
        mc.worldRenderer.reload();
    }

    private void restoreOriginalSettings() {
        mc.options.getViewDistance().setValue(origRenderDistance);
        mc.options.getSimulationDistance().setValue(origSimulationDistance);
        mc.options.getEnableVsync().setValue(origVsync);
        mc.options.getEntityShadows().setValue(origEntityShadows);
        mc.options.getBobView().setValue(origViewBobbing);
        mc.options.getFov().setValue((int) origFov);
        mc.options.getMaxFps().setValue(origMaxFps);
        mc.options.getGraphicsMode().setValue(origGraphics);
        mc.options.getCloudRenderMode().setValue(origClouds);
        mc.options.getParticles().setValue(origParticles);
        mc.options.getEntityDistanceScaling().setValue(origEntityDistance);
        mc.options.getAo().setValue(origSmoothLighting);
        mc.options.getMipmapLevels().setValue(origMipmapLevels);
        mc.options.getDistortionEffectScale().setValue(origDistortionEffects);
        mc.options.getGlintSpeed().setValue(origGlintSpeed);
        mc.options.getDamageTiltStrength().setValue(origDamageTilt);
        mc.options.getShowAutosaveIndicator().setValue(origAutosaveIndicator);

        if (fullscreen.isValue() && mc.getWindow().isFullscreen()) {
            mc.getWindow().toggleFullscreen();
        }

        mc.options.write();
        mc.worldRenderer.reload();
    }
}