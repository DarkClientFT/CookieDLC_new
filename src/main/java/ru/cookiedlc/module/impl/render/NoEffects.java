package ru.cookiedlc.module.impl.render;

import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.MultiSelectSetting;
import ru.cookiedlc.common.util.other.Instance;

import java.util.Arrays;

public class NoEffects extends Module {

    public static NoEffects getInstance() {
        return Instance.get(NoEffects.class);
    }

    public MultiSelectSetting modeListSetting = new MultiSelectSetting("Убирать", "Эффекты, которые будут убраны")
            .value("Ночное зрение", "Тьма", "Свечение", "Слепота", "Тошнота");

    public NoEffects() {
        super("NoEffects", "No Effects", ModuleCategory.RENDER);
        setup(modeListSetting);
        modeListSetting.getSelected().addAll(Arrays.asList("Тьма", "Ночное зрение", "Слепота", "Тошнота"));
    }
}