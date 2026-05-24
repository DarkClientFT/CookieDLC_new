package ru.cookiedlc.common.trait;

import ru.cookiedlc.module.api.setting.Setting;

public interface Setupable {
    void setup(Setting... settings);
}