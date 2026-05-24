package ru.cookiedlc.api.system.discord.callbacks;

import com.sun.jna.Callback;
import ru.cookiedlc.api.system.discord.utils.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(DiscordUser var1);
}