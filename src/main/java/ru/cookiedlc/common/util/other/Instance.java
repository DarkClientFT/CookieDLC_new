package ru.cookiedlc.common.util.other;

import lombok.experimental.UtilityClass;
import ru.cookiedlc.ui.hud.api.AbstractDraggable;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.core.Main;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@UtilityClass
public class Instance {
    private final ConcurrentMap<Class<? extends Module>, Module> instanceModules = new ConcurrentHashMap<>();
    private final ConcurrentMap<Class<? extends AbstractDraggable>, AbstractDraggable> instanceDraggables = new ConcurrentHashMap<>();

    public <T extends Module> T get(Class<T> clazz) {
        return clazz.cast(instanceModules.computeIfAbsent(clazz, instance -> Main.getInstance().getModuleProvider().get(instance)));
    }

    public <T extends Module> T get(String module) {
        return Main.getInstance().getModuleProvider().get(module);
    }

    public <T extends AbstractDraggable> T getDraggable(Class<T> clazz) {
        return clazz.cast(instanceDraggables.computeIfAbsent(clazz, instance -> Main.getInstance().getDraggableRepository().get(instance)));
    }

    public <T extends AbstractDraggable> T getDraggable(String draggable) {
        return Main.getInstance().getDraggableRepository().get(draggable);
    }
}
