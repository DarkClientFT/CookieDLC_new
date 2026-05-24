package ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.manager;

import lombok.Getter;
import net.minecraft.util.Formatting;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.cookiedlc.common.QuickLogger;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.TelegramNotifier;
import ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.bot.AutoBuyBot;

public class TelegramBotManager implements QuickLogger {
    private static final TelegramBotManager INSTANCE = new TelegramBotManager();
    
    @Getter
    private static AutoBuyBot bot;
    private static TelegramBotsApi botsApi;
    
    @Getter
    private static boolean running = false;
    
    private TelegramBotManager() {}
    
    public static TelegramBotManager getInstance() {
        return INSTANCE;
    }
    
    public void addBot(String token, String chatId) {
        AutoBuyData data = AutoBuyData.getInstance();
        data.setBotToken(token);
        data.setChatId(chatId);
        data.setBotEnabled(true);
        
        INSTANCE.logDirect("Telegram бот добавлен! Используйте .autobuy tg start для запуска", Formatting.GREEN);
    }
    
    public void startBot() {
        if (running) {
            INSTANCE.logDirect("Telegram бот уже запущен!", Formatting.YELLOW);
            return;
        }
        
        AutoBuyData data = AutoBuyData.getInstance();
        
        if (data.getBotToken().isEmpty() || data.getChatId().isEmpty()) {
            INSTANCE.logDirect("Сначала добавьте бота: .autobuy tg add <token> <chatId>", Formatting.RED);
            return;
        }
        
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
            bot = new AutoBuyBot(data.getBotToken(), data.getChatId());
            botsApi.registerBot(bot);
            running = true;
            
            INSTANCE.logDirect("Telegram бот успешно запущен!", Formatting.GREEN);
            TelegramNotifier.sendMessage("✅ *Бот подключен!*\n\nАвтобай готов к работе.\nИспользуйте /help для списка команд.");
            
        } catch (TelegramApiException e) {
            INSTANCE.logDirect("Ошибка запуска бота: " + e.getMessage(), Formatting.RED);
            running = false;
        }
    }
    
    public void stopBot() {
        if (!running) {
            INSTANCE.logDirect("Telegram бот не запущен!", Formatting.YELLOW);
            return;
        }
        
        try {
            if (bot != null) {
                TelegramNotifier.sendMessage("⛔ *Бот отключен*\n\nСоединение закрыто.");
                bot.onClosing();
                bot = null;
            }
            running = false;
            INSTANCE.logDirect("Telegram бот остановлен!", Formatting.GREEN);
        } catch (Exception e) {
            INSTANCE.logDirect("Ошибка остановки бота: " + e.getMessage(), Formatting.RED);
        }
    }
    
    public void removeBot() {
        stopBot();
        AutoBuyData data = AutoBuyData.getInstance();
        data.setBotToken("");
        data.setChatId("");
        data.setBotEnabled(false);
        INSTANCE.logDirect("Telegram бот удален из конфига!", Formatting.GREEN);
    }
    
    public boolean isConfigured() {
        AutoBuyData data = AutoBuyData.getInstance();
        return !data.getBotToken().isEmpty() && !data.getChatId().isEmpty();
    }
}
