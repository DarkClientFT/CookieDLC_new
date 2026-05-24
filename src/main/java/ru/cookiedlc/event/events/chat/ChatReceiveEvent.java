package ru.cookiedlc.event.events.chat;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.text.Text;
import ru.cookiedlc.event.api.events.Event;

@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatReceiveEvent implements Event {
    Text message;
}