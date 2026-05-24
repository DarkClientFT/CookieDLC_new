package ru.cookiedlc.core.listener;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.api.auth.protect.CookieProtect;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.core.listener.impl.EventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
@CookieProtect
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ListenerRepository {
    final List<Listener> listeners = new ArrayList<>();
    
    public void setup() {
        registerListeners(new EventListener());
    }

    public void registerListeners(Listener... listeners) {
        this.listeners.addAll(List.of(listeners));
        Arrays.stream(listeners).forEach(listener -> Main.getInstance().getEventManager().register(listener));
    }
}
