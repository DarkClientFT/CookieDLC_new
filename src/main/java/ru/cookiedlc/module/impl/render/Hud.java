package ru.cookiedlc.module.impl.render;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.ColorSetting;
import ru.cookiedlc.module.api.setting.implement.MultiSelectSetting;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.common.util.other.Instance;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Hud extends Module {
    public static Hud getInstance() {
        return Instance.get(Hud.class);
    }

    public BooleanSetting useBlur = new BooleanSetting("Use Blur", "Enable blur effect for GUI and HUD elements")
            .setValue(false);

    public MultiSelectSetting interfaceSettings = new MultiSelectSetting("Elements", "Customize the interface elements")
                .value("Watermark", "Hot Keys", "Potions", "Staff List", "Target Hud", "Cool Downs", "Notifications");

    public MultiSelectSetting notificationSettings = new MultiSelectSetting("Notifications", "Choose when the notification will appear")
            .value("Module Switch", "Staff Join", "Item Pick Up", "Auto Armor", "Break Shield").visible(()-> interfaceSettings.isSelected("Notifications"));

    public ColorSetting colorSetting = new ColorSetting("Client Color", "Select your client's color")
            .setColor(0xFF6C9AFD).presets(0xFF6C9AFD, 0xFF8C7FFF, 0xFFFFA576, 0xFFFF7B7B);
    
    public Hud() {
        super("Hud", ModuleCategory.RENDER);
        setup(useBlur, colorSetting, interfaceSettings, notificationSettings);
    }


}
