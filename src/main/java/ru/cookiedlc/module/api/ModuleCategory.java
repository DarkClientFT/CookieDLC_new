package ru.cookiedlc.module.api;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ModuleCategory {
    COMBAT("Combat", "b"),
    MOVEMENT("Movement", "g"),
    RENDER("Render", "c"),
    PLAYER("Player", "f"),
    MISC("Misc", "l");

    private final String readableName;
    private final String iconChar;
}
