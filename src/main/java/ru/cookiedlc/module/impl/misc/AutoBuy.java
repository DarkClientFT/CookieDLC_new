package ru.cookiedlc.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.module.api.setting.implement.BindSetting;
import ru.cookiedlc.module.api.setting.implement.BooleanSetting;
import ru.cookiedlc.module.api.setting.implement.ValueSetting;
import ru.cookiedlc.event.events.container.CloseScreenEvent;
import ru.cookiedlc.event.events.keyboard.KeyEvent;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.event.events.render.DrawEvent;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.manager.BuyingManager;
import ru.cookiedlc.module.impl.misc.autobuy.holy.manager.PricesManager;
import ru.cookiedlc.module.impl.misc.autobuy.holy.manager.SellManager;
import ru.cookiedlc.module.impl.misc.autobuy.holy.model.TickDelayer;
import ru.cookiedlc.module.impl.misc.autobuy.holy.render.AutoBuyScreen;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AutoBuy extends Module {

    BindSetting menuBind = new BindSetting("Open Menu", "Key to open AutoBuy menu");

    ValueSetting multiplierSetting = new ValueSetting("Multiplier", "Price multiplier for buying")
            .setValue(0.65f).range(0.1f, 1.0f);

    ValueSetting refreshDelaySetting = new ValueSetting("Refresh Delay", "Auction refresh delay in ticks (10-30)")
            .setValue(30f).range(10f, 30f);

    BooleanSetting notificationsSetting = new BooleanSetting("Notifications", "Show notifications when buying")
            .setValue(true);

    AutoBuyData data = AutoBuyData.getInstance();

    public AutoBuy() {
        super("AutoBuy", "Auto Buy", ModuleCategory.MISC);
        setup(menuBind, multiplierSetting, refreshDelaySetting, notificationsSetting);
    }

    @Override
    public void activate() {
        data.setMultiplier(multiplierSetting.getValue());
        data.setRefreshDelay((int) refreshDelaySetting.getValue());
    }

    @Override
    public void deactivate() {
        stopAll();
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (mc.player == null) return;
        if (e.isKeyDown(menuBind.getKey())) {
            openMenu();
        }
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        data.setMultiplier(multiplierSetting.getValue());
        data.setRefreshDelay((int) refreshDelaySetting.getValue());
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (!e.getType().equals(PacketEvent.Type.RECEIVE)) return;
        if (!(e.getPacket() instanceof OpenScreenS2CPacket)) return;

        TickDelayer.runTaskLater(() -> {
            if (mc.currentScreen instanceof GenericContainerScreen screen) {
                BuyingManager.onScreenOpen(screen);
                PricesManager.onScreenOpen(screen);
            }
        }, 1);
    }

    @EventHandler
    public void onCloseScreen(CloseScreenEvent e) {
        if (e.getScreen() instanceof GenericContainerScreen screen) {
            String title = screen.getTitle().getString();
            if (title.startsWith("Аукцион (")) {
                if (BuyingManager.isListen() || BuyingManager.isAwaitingConfirm()) {
                    BuyingManager.stop();
                    data.setStatus("N/A");
                }
                if (PricesManager.isListen()) {
                    PricesManager.stop();
                    data.setStatus("N/A");
                }
            }
        }
    }

    public void openMenu() {
        if (mc.currentScreen != null) return;
        mc.setScreen(new AutoBuyScreen(
                this::startBuying,
                this::startGettingPrices,
                this::startSelling
        ));
    }

    public void startBuying() {
        BuyingManager.start();
    }

    public void startGettingPrices() {
        PricesManager.start();
    }

    public void startSelling() {
        SellManager.start();
    }

    public void stopAll() {
        BuyingManager.stop();
        SellManager.stop();
        PricesManager.stop();
        data.setStatus("N/A");
    }
}
