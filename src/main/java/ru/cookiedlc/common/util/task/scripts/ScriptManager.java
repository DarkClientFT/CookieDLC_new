package ru.cookiedlc.common.util.task.scripts;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ScriptManager {
    private final Map<String, Script> scripts = new ConcurrentHashMap<>();

    public Optional<Script> getScript(String name) {
        return isNullOrEmpty(name) ? Optional.empty() : Optional.of(scripts.computeIfAbsent(name, x -> new Script()));
    }

    public Script addScript(String name, Script script) {
        if (isNullOrEmpty(name) || script == null) {
            throw new IllegalArgumentException("Script name or instance cannot be null or empty");
        }
        return scripts.put(name, script);
    }

    public boolean containsScript(String name) {
        return !isNullOrEmpty(name) && scripts.containsKey(name);
    }

    public boolean finished(String name) {
        return !isNullOrEmpty(name) && getScript(name).isPresent() && getScript(name).get().isFinished();
    }

    public void removeScript(String name) {
        if (!isNullOrEmpty(name)) {
            scripts.remove(name);
        }
    }

    public void cleanupScript(String name) {
        if (!isNullOrEmpty(name)) {
            scripts.computeIfPresent(name, (k, v) -> {
                v.cleanup();
                return v;
            });
        }
    }

    public void cleanupAll() {
        scripts.forEach((k, v) -> v.cleanup());
    }

    public void clearAll() {
        scripts.clear();
    }

    public void updateScript(String name) {
        updateScript(name, () -> true);
    }

    public void updateScript(String name, Supplier<Boolean> condition) {
        if (condition.get() && !isNullOrEmpty(name)) {
            scripts.computeIfPresent(name, (k, v) -> {
                v.update();
                return v;
            });
        }
    }

    public void updateAll() {
        scripts.values().forEach(Script::update);
    }

    public Set<String> getAllScriptNames() {
        return Collections.unmodifiableSet(scripts.keySet());
    }

    public Map<String, Script> getAllScripts() {
        return Collections.unmodifiableMap(scripts);
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
