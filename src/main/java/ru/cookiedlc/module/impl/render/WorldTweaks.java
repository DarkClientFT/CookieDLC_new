package ru.cookiedlc.module.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.MultiSelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.common.util.color.ColorUtil;
import ru.cookiedlc.common.util.other.Instance;
import ru.cookiedlc.event.events.render.FogEvent;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorldTweaks extends Module {
    public static WorldTweaks getInstance() {
        return Instance.get(WorldTweaks.class);
    }

    public final MultiSelectSetting modeSetting = new MultiSelectSetting("World Setting", "Allows you to customize world")
            .value("Bright", "Time", "Fog");

    public final ValueSetting brightSetting = new ValueSetting("Bright", "Sets the value of the maximum bright")
            .setValue(1.0F).range(0.0F, 1.0F).visible(() -> modeSetting.isSelected("Bright"));

    public final ValueSetting timeSetting = new ValueSetting("Time", "Sets the value of the time")
            .setValue(12).range(0, 24).visible(() -> modeSetting.isSelected("Time"));

    public final ValueSetting distanceSetting = new ValueSetting("Fog Distance", "Sets the value of the time")
            .setValue(100).range(20, 200).visible(() -> modeSetting.isSelected("Fog"));

    public WorldTweaks() {
        super("WorldTweaks", "World Tweaks", ModuleCategory.RENDER);
        setup(modeSetting, brightSetting, timeSetting, distanceSetting);
    }

    @Override
    public void deactivate() {
        super.deactivate();
    }

    @EventHandler
    public void onFog(FogEvent e) {
        if (modeSetting.isSelected("Fog")) {
            e.setDistance(distanceSetting.getValue());
            e.setColor(ColorUtil.getClientColor());
            e.cancel();
        }
    }
}
