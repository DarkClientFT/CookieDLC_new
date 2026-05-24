package ru.cookiedlc.module.impl.misc;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.event.events.player.TickEvent;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.MultiSelectSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.module.impl.movement.air.PacketEvent;

public class BlockFly extends Module {
    private static final Logger LOGGER = LoggerFactory.getLogger("BlockFly");

    final MultiSelectSetting mode = new MultiSelectSetting("Mode", "Режим работы BlockFly")
            .value("Smart", "Normal");

    final ValueSetting intervalMs = new ValueSetting("Interval", "Интервал между пакетами (мс)")
            .setValue(500f).range(50f, 5000f);

    final ValueSetting maxQueueSize = new ValueSetting("MaxQueueSize", "Максимальный размер очереди")
            .setValue(1000f).range(100f, 5000f);

    final BooleanSetting debug = new BooleanSetting("Debug", "Включить дебаг логи")
            .setValue(false);

    private final Queue<Packet<?>> storedPackets = new LinkedList<>();
    private final AtomicBoolean sending = new AtomicBoolean(false);
    private final Random random = new Random();

    private long lastPulseTime = 0L;
    private int currentPulseDelayMs = 0;
    private int totalPacketsSent = 0;
    private int totalPacketsQueued = 0;

    public BlockFly() {
        super("BlockFly", "BlockFly", ModuleCategory.MISC);
        setup(mode, intervalMs, maxQueueSize, debug);
    }

    @Override
    public void activate() {
        if (mc.player == null || mc.world == null || mc.isIntegratedServerRunning() || mc.getNetworkHandler() == null) {
            deactivate();
            return;
        }

        storedPackets.clear();
        sending.set(false);
        lastPulseTime = System.currentTimeMillis();
        currentPulseDelayMs = mode.isSelected("Smart") ? rand(150, 250) : (int) intervalMs.getValue();
        totalPacketsSent = 0;
        totalPacketsQueued = 0;

        if (debug.isValue()) {
            LOGGER.info("BlockFly: Модуль включен, режим: {}", mode.getSelected());
        }
    }

    @Override
    public void deactivate() {
        if (mc.world == null || mc.player == null) return;

        sending.set(true);
        int flushedCount = flushPackets();
        sending.set(false);

        if (debug.isValue()) {
            LOGGER.info("BlockFly: Модуль выключен");
            LOGGER.info("BlockFly: Статистика - Всего пакетов в очереди: {}", totalPacketsQueued);
            LOGGER.info("BlockFly: Статистика - Всего пакетов отправлено: {}", totalPacketsSent);
            LOGGER.info("BlockFly: Статистика - Пакетов сброшено при выключении: {}", flushedCount);
        }
    }

    @EventHandler
    public void onUpdate(TickEvent event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (now - lastPulseTime >= currentPulseDelayMs && !storedPackets.isEmpty()) {
            flushPackets();
            lastPulseTime = now;
            currentPulseDelayMs = mode.isSelected("Smart") ? rand(150, 250) : (int) intervalMs.getValue();
        }
    }

    private int flushPackets() {
        if (mc.player == null) return 0;

        sending.set(true);
        int packetCount = storedPackets.size();
        int flushedCount = 0;

        while (!storedPackets.isEmpty()) {
            Packet<?> packet = storedPackets.poll();
            mc.player.networkHandler.sendPacket(packet);
            totalPacketsSent++;
            flushedCount++;
        }

        sending.set(false);

        if (debug.isValue() && packetCount > 0) {
            LOGGER.info("BlockFly: Отправлена пачка пакетов - Количество: {}", packetCount);
        }

        return flushedCount;
    }

    private int rand(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    @EventHandler
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null || sending.get()) return;

        Packet<?> packet = event.getPacket();
        if (packet instanceof ClientStatusC2SPacket || packet instanceof KeepAliveC2SPacket) {
            return;
        }

        if (storedPackets.size() >= (int) maxQueueSize.getValue()) {
            if (debug.isValue()) {
                LOGGER.warn("BlockFly: Очередь переполнена, отправляем пакет немедленно");
            }
            return;
        }

        event.cancel();
        storedPackets.add(packet);
        totalPacketsQueued++;

        if (debug.isValue() && totalPacketsQueued % 50 == 0) {
            LOGGER.info("BlockFly: Пакет добавлен в очередь - Всего в очереди: {}, Всего добавлено: {}",
                    storedPackets.size(), totalPacketsQueued);
        }
    }
}