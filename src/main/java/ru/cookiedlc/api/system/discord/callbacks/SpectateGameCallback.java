package ru.cookiedlc.api.system.discord.callbacks;

import com.sun.jna.Callback;

public interface SpectateGameCallback extends Callback {
    void apply(String var1);
}