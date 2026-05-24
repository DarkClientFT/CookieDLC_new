package ru.cookiedlc.module.impl.misc.autobuy.holy.antinetvision;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Formatting;
import ru.cookiedlc.common.QuickLogger;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.model.TickDelayer;
import ru.cookiedlc.module.impl.misc.autobuy.holy.manager.BuyingManager;

import java.util.Random;

public class AntiDetect implements QuickLogger {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final AntiDetect INSTANCE = new AntiDetect();
    private static final Random random = new Random();
    private static final String[] CHAT_MESSAGES = {
            "йоу пацаны стикдев хуета кунидев круче",
            "Продам альтуху за 50 отцов рейдлайта",
            "Вы же знали что чмоил хуесос и уебище",
            "ЧМОИЛ ХУЕСОС!! ЕСЛИ ВЫ СОГЛСАСНЫ + В ЧАТ СУКААА",
            "Я хуесос",
            "Всем привет!",
            "Народ, давайте закроем фичадев пж",
            "Хай всем на связи куки длс ",
            "АННУУУССС ",
            "ЧЛЕН ПИСЬКИ"
    };

    @Getter
    @Setter
    private static boolean onBreak = false;

    private static long lastBreakTime = 0;
    private static long nextBreakInterval = 0;

    private static final int MIN_INTERVAL_TICKS = 5 * 60 * 20;
    private static final int MAX_INTERVAL_TICKS = 10 * 60 * 20;

    private static int walkTicks = 0;
    private static int breakPhase = 0;

    public static void init() {
        lastBreakTime = System.currentTimeMillis();
        nextBreakInterval = getRandomInterval();
        onBreak = false;
        breakPhase = 0;
        walkTicks = 0;
    }

    public static void reset() {
        lastBreakTime = System.currentTimeMillis();
        nextBreakInterval = getRandomInterval();
        onBreak = false;
        breakPhase = 0;
        walkTicks = 0;
    }

    private static long getRandomInterval() {

        return (5 * 60 * 1000) + random.nextInt(5 * 60 * 1000);
    }

    public static boolean shouldTakeBreak() {
        if (!AutoBuyData.getInstance().isAntiDetectEnabled()) return false;
        if (onBreak) return true;

        long elapsed = System.currentTimeMillis() - lastBreakTime;
        return elapsed >= nextBreakInterval;
    }

    public static void startBreak() {
        if (onBreak) return;

        onBreak = true;
        breakPhase = 0;
        walkTicks = 0;

        INSTANCE.logDirect(Formatting.YELLOW + "[AntiDetect] " + Formatting.WHITE + "Делаю перерыв...");

        if (mc.player != null) {
            mc.player.closeHandledScreen();
        }

        runBreakPhase();
    }

    private static void runBreakPhase() {
        if (mc.player == null) {
            finishBreak();
            return;
        }

        switch (breakPhase) {
            case 0 -> {

                TickDelayer.runTaskLater(() -> {
                    breakPhase = 1;
                    runBreakPhase();
                }, 20 + random.nextInt(20), "antidetect");
            }
            case 1 -> {

                startWalking();
            }
            case 2 -> {

                TickDelayer.runTaskLater(() -> {
                    breakPhase = 3;
                    runBreakPhase();
                }, 20 + random.nextInt(40), "antidetect");
            }
            case 3 -> {

                sendRandomMessage();
                TickDelayer.runTaskLater(() -> {
                    breakPhase = 4;
                    runBreakPhase();
                }, 40 + random.nextInt(60), "antidetect");
            }
            case 4 -> {

                finishBreak();
            }
        }
    }

    private static void startWalking() {
        if (mc.player == null || mc.options == null) {
            breakPhase = 2;
            runBreakPhase();
            return;
        }

        mc.options.forwardKey.setPressed(true);
        walkTicks = 0;

        int walkDuration = 40 + random.nextInt(20);

        walkStep(walkDuration);
    }

    private static void walkStep(int remainingTicks) {
        if (remainingTicks <= 0 || mc.player == null) {

            if (mc.options != null) {
                mc.options.forwardKey.setPressed(false);
            }
            breakPhase = 2;
            runBreakPhase();
            return;
        }

        TickDelayer.runTaskLater(() -> walkStep(remainingTicks - 1), 1, "antidetect");
    }

    private static void sendRandomMessage() {
        if (mc.player == null) return;

        String message = CHAT_MESSAGES[random.nextInt(CHAT_MESSAGES.length)];
        mc.player.networkHandler.sendChatMessage(message);

        INSTANCE.logDirect(Formatting.YELLOW + "[AntiDetect] " + Formatting.GRAY + "Отправлено: " + message);
    }

    private static void finishBreak() {
        onBreak = false;
        breakPhase = 0;
        lastBreakTime = System.currentTimeMillis();
        nextBreakInterval = getRandomInterval();

        INSTANCE.logDirect(Formatting.GREEN + "[AntiDetect] " + Formatting.WHITE + "Перерыв окончен, продолжаю автобай");

        BuyingManager.start();
    }

    public static void stop() {
        onBreak = false;
        breakPhase = 0;
        if (mc.options != null) {
            mc.options.forwardKey.setPressed(false);
        }
        TickDelayer.stopAllTasks("antidetect");
    }
}
