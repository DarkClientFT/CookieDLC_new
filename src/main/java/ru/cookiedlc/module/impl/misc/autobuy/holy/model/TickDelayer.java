package ru.cookiedlc.module.impl.misc.autobuy.holy.model;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import java.util.ArrayList;
import java.util.List;

public class TickDelayer {
    private static final List<DelayedTask> tasks = new ArrayList<>();
    private static boolean initialized = false;

    public static void runTaskLater(Runnable task, int delayTicks) {
        runTaskLater(task, delayTicks, "empty");
    }

    public static void runTaskLater(Runnable task, int delayTicks, String name) {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        tasks.add(new DelayedTask(task, delayTicks, name));
    }

    public static void stopAllTasks(String name) {
        if (name == null) return;
        tasks.removeIf(task -> name.equals(task.name));
    }

    private static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            List<DelayedTask> tasksToProcess = new ArrayList<>(tasks);
            tasks.clear();

            for (DelayedTask task : tasksToProcess) {
                if (task.delayTicks <= 0) {
                    try {
                        task.task.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    task.delayTicks--;
                    tasks.add(task);
                }
            }
        });
    }

    private static class DelayedTask {
        final Runnable task;
        int delayTicks;
        final String name;

        DelayedTask(Runnable task, int delayTicks, String name) {
            this.task = task;
            this.delayTicks = delayTicks;
            this.name = name;
        }
    }
}
