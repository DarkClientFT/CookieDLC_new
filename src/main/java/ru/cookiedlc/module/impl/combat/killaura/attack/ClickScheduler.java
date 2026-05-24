package ru.cookiedlc.module.impl.combat.killaura.attack;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.common.QuickImports;
import ru.cookiedlc.common.util.world.ServerUtil;
import ru.cookiedlc.core.Main;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClickScheduler implements QuickImports {
    private final int[] funTimeTicks = new int[]{10, 11, 10, 11, 10, 12, 11, 10};
    private final int[] spookyTicks = new int[]{10, 11, 10, 11, 12, 10, 11, 10, 12, 11};
    private final int[] defaultTicks = new int[]{10, 11, 10, 11};
    long lastClickTime = System.currentTimeMillis();
    private int lastTickIndex = 0;

    public boolean isCooldownComplete(boolean dynamicCooldown, int ticks) {
        float cooldownProgress = mc.player.getAttackCooldownProgress(ticks);
        if (!dynamicCooldown) {
            return cooldownProgress > 0.9F;
        }
        int requiredTicks = tickCount();
        boolean ticksPassed = hasTicksElapsedSinceLastClick(requiredTicks - ticks);
        return (ticksPassed && cooldownProgress > 0.85F) || cooldownProgress > 0.93F;
    }

    public boolean hasTicksElapsedSinceLastClick(int ticks) {
        if (ticks <= 0) return true;
        float tpsMultiplier = Math.max(0.5F, 20F / Math.max(1F, ServerUtil.TPS));
        return lastClickPassed() >= (ticks * 50L * tpsMultiplier);
    }

    public long lastClickPassed() {
        return System.currentTimeMillis() - lastClickTime;
    }

    public void recalculate() {
        lastClickTime = System.currentTimeMillis();
        int[] currentArray = getCurrentTickArray();
        lastTickIndex = (lastTickIndex + 1) % currentArray.length;
    }

    int tickCount() {
        int[] currentArray = getCurrentTickArray();
        int count = Main.getInstance().getAttackPerpetrator().getAttackHandler().getCount();
        return currentArray[count % currentArray.length];
    }
    
    private int[] getCurrentTickArray() {
        return switch (ServerUtil.server) {
            case "FunTime" -> funTimeTicks;
            case "SpookyTime" -> spookyTicks;
            default -> defaultTicks;
        };
    }
}
