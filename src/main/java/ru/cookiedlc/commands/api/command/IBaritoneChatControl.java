package ru.cookiedlc.commands.api.command;

import java.util.UUID;

public interface IBaritoneChatControl {
    String FORCE_COMMAND_PREFIX = String.format("<<%s>>", UUID.randomUUID());
}
