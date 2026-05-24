package ru.cookiedlc.module.impl;

import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.event.events.render.WorldRenderEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.*;

public class ExampleModule extends Module {

    BindSetting bindSetting = new BindSetting("Name settings", "Discription");

    ValueSetting valueSetting = new ValueSetting("Name settings", "Discription")
            .setValue(0.65f).range(0.1f, 1.0f);
    BooleanSetting booleanSetting = new BooleanSetting("Name settings", "Discription")
            .setValue(true);

    BindSetting exampleBind1 = new BindSetting("Name settings", "Discription");

    BooleanSetting exampleBoolean1 = new BooleanSetting("Name settings", "Discription")
            .setValue(true);

    GroupSetting groupSetting = new GroupSetting("Name settings", "Discription")
            .settings(exampleBind1, exampleBoolean1)
            .setValue(true);

    ColorSetting colorSetting = new ColorSetting("Name settings", "Discription")
            .setColor(0xFF6C9AFD).presets(0xFF6C9AFD, 0xFF8C7FFF, 0xFFFFA576, 0xFFFF7B7B);

    MultiSelectSetting multiSelectSetting = new MultiSelectSetting("Name settings", "Discription")
            .value("Example1", "Example2");

    BooleanSetting exampleDepend = new BooleanSetting("Name settings", "Discription")
            .setValue(false);

    TextSetting textSetting = new TextSetting("Name settings", "Discription")
            .setText("Example")
            .visible(() -> exampleDepend.isValue());

    public ExampleModule() {
        super("ExampleModule","ExampleModule", ModuleCategory.MISC);
        setup(bindSetting, valueSetting, booleanSetting, groupSetting, colorSetting, multiSelectSetting, textSetting);
    }

    @Override
    public void activate() {
        super.activate();
    }
    @Override
    public void deactivate() {
        super.deactivate();
    }
}
