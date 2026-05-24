package ru.cookiedlc.ui.clickgui.components.implement.settings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.cookiedlc.module.api.setting.Setting;
import ru.cookiedlc.ui.clickgui.components.AbstractComponent;

@Getter
@RequiredArgsConstructor
public abstract class AbstractSettingComponent extends AbstractComponent {
    private final Setting setting;
}

