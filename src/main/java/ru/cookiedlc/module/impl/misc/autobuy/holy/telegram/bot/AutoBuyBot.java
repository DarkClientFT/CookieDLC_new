package ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.bot;

import net.minecraft.client.MinecraftClient;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.cookiedlc.module.impl.misc.autobuy.holy.antinetvision.AntiDetect;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.manager.BuyingManager;
import ru.cookiedlc.module.impl.misc.autobuy.holy.manager.PricesManager;
import ru.cookiedlc.module.impl.misc.autobuy.holy.manager.SellManager;
import ru.cookiedlc.module.impl.misc.autobuy.holy.model.TickDelayer;

import java.util.ArrayList;
import java.util.List;

public class AutoBuyBot extends TelegramLongPollingBot {
    
    private final String botToken;
    private final String allowedChatId;
    private final MinecraftClient mc = MinecraftClient.getInstance();
    
    public AutoBuyBot(String botToken, String chatId) {
        super(botToken);
        this.botToken = botToken;
        this.allowedChatId = chatId;
    }
    
    @Override
    public String getBotUsername() {
        return "AutoBuyBot";
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }
    
    private void handleMessage(Update update) {
        String chatId = update.getMessage().getChatId().toString();
        
        if (!chatId.equals(allowedChatId)) {
            sendText(chatId, "❌ У вас нет доступа к этому боту.");
            return;
        }
        
        String messageText = update.getMessage().getText();
        String[] parts = messageText.split(" ");
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "/start", "/help" -> sendHelp(chatId);
            case "/status" -> sendStatus(chatId);
            case "/balance" -> sendBalance(chatId);
            case "/hub" -> executeHub(chatId);
            case "/getprices" -> executeGetPrices(chatId);
            case "/pause" -> executePause(chatId);
            case "/resume" -> executeResume(chatId);
            case "/break" -> executeBreak(chatId);
            case "/history" -> sendHistory(chatId);
            case "/stats" -> sendStats(chatId);
            case "/menu" -> sendMainMenu(chatId);
            default -> sendText(chatId, "❓ Неизвестная команда. Используйте /help");
        }
    }
    
    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String chatId = callbackQuery.getMessage().getChatId().toString();
        String data = callbackQuery.getData();
        
        if (!chatId.equals(allowedChatId)) return;
        
        switch (data) {
            case "status" -> sendStatus(chatId);
            case "balance" -> sendBalance(chatId);
            case "pause" -> executePause(chatId);
            case "resume" -> executeResume(chatId);
            case "hub" -> executeHub(chatId);
            case "break" -> executeBreak(chatId);
            case "getprices" -> executeGetPrices(chatId);
            case "history" -> sendHistory(chatId);
            case "stats" -> sendStats(chatId);
            case "menu" -> sendMainMenu(chatId);
        }
    }
    
    private void sendHelp(String chatId) {
        String text = """
            🤖 *AutoBuy Telegram Bot*
            
            *Команды:*
            /status - Текущий статус автобая
            /balance - Баланс игрока
            /hub - Выйти в хаб
            /getprices - Обновить цены
            /pause - Поставить на паузу
            /resume - Возобновить автобай
            /break - Начать перерыв
            /history - История покупок
            /stats - Статистика
            /menu - Главное меню с кнопками
            
            *Используйте кнопки для быстрого доступа!*
            """;
        
        sendTextWithKeyboard(chatId, text);
    }
    
    private void sendStatus(String chatId) {
        AutoBuyData data = AutoBuyData.getInstance();
        
        String statusEmoji = switch (data.getStatus()) {
            case "Покупка" -> "🛒";
            case "Получение цен" -> "📈";
            case "Продажа" -> "💰";
            default -> "⏸️";
        };
        
        String text = String.format("""
            %s *Статус AutoBuy*
            
            📊 *Статус:* %s
            🔢 *Множитель:* %.2f
            📦 *Загружено цен:* %d
            ✅ *Куплено предметов:* %d
            💳 *Баланс:* %,d монет
            🤖 *AntiDetect:* %s
            """,
            statusEmoji,
            data.getStatus(),
            data.getMultiplier(),
            data.getPriceForOne().size(),
            data.getItemsBought(),
            data.getMoney(),
            data.isAntiDetectEnabled() ? "Включен" : "Выключен"
        );
        
        sendTextWithKeyboard(chatId, text);
    }
    
    private void sendBalance(String chatId) {
        AutoBuyData data = AutoBuyData.getInstance();
        int balance = data.getMoney();
        
        String text = String.format("""
            💰 *Баланс игрока*
            
            💳 *Текущий баланс:* %,d монет
            📊 *Статус:* %s
            """,
            balance,
            data.getStatus()
        );
        
        sendTextWithKeyboard(chatId, text);
    }
    
    private void executeHub(String chatId) {
        if (mc.player == null) {
            sendText(chatId, "❌ Игрок не в сети!");
            return;
        }
        
        BuyingManager.stop();
        SellManager.stop();
        PricesManager.stop();
        AutoBuyData.getInstance().setStatus("N/A");
        
        if (mc.player.currentScreenHandler != null) {
            mc.player.closeHandledScreen();
        }
        
        TickDelayer.runTaskLater(() -> {
            if (mc.player != null) {
                mc.player.networkHandler.sendChatMessage("/hub");
            }
        }, 5);
        
        sendText(chatId, "🏠 *Выход в хаб*\n\nАвтобай остановлен, возвращаемся в хаб...");
    }
    
    private void executeGetPrices(String chatId) {
        if (mc.player == null) {
            sendText(chatId, "❌ Игрок не в сети!");
            return;
        }
        
        BuyingManager.stop();
        SellManager.stop();
        
        sendText(chatId, "📈 *Обновление цен*\n\nНачинаю получение рыночных цен...");
        
        TickDelayer.runTaskLater(() -> {
            PricesManager.start();
        }, 10);
    }
    
    private void executePause(String chatId) {
        BuyingManager.stop();
        SellManager.stop();
        AutoBuyData.getInstance().setStatus("N/A");
        
        sendText(chatId, "⏸️ *Автобай приостановлен*\n\nВсе процессы остановлены.");
    }
    
    private void executeResume(String chatId) {
        AutoBuyData data = AutoBuyData.getInstance();
        
        if (data.getPriceForOne().isEmpty()) {
            sendText(chatId, "❌ *Ошибка*\n\nСначала получите цены командой /getprices");
            return;
        }
        
        if (mc.player == null) {
            sendText(chatId, "❌ Игрок не в сети!");
            return;
        }
        
        BuyingManager.start();
        sendText(chatId, "▶️ *Автобай возобновлен*\n\nПродолжаю покупку предметов...");
    }
    
    private void executeBreak(String chatId) {
        if (!AutoBuyData.getInstance().isAntiDetectEnabled()) {
            sendText(chatId, "❌ *AntiDetect отключен*\n\nВключите AntiDetect для использования перерывов.");
            return;
        }
        
        AntiDetect.startBreak();
        sendText(chatId, "💤 *Перерыв начат*\n\nБот имитирует человеческое поведение...");
    }
    
    private void sendHistory(String chatId) {
        AutoBuyData data = AutoBuyData.getInstance();
        List<AutoBuyData.BuyHistory> history = data.getHistory();
        
        if (history.isEmpty()) {
            sendText(chatId, "📜 *История покупок*\n\nИстория пуста.");
            return;
        }
        
        StringBuilder text = new StringBuilder("📜 *История покупок* (последние 10)\n\n");
        
        int start = Math.max(0, history.size() - 10);
        for (int i = history.size() - 1; i >= start; i--) {
            AutoBuyData.BuyHistory entry = history.get(i);
            double coef = (double) entry.auctionPrice() / entry.price();
            text.append(String.format(
                "📦 *%s*\n" +
                "💰 Цена: %,d (x%.2f)\n" +
                "💳 Баланс: %,d → %,d\n\n",
                entry.itemType().getName(),
                entry.allPrice(),
                coef,
                entry.balance(),
                entry.balance() - entry.allPrice()
            ));
        }
        
        sendTextWithKeyboard(chatId, text.toString());
    }
    
    private void sendStats(String chatId) {
        AutoBuyData data = AutoBuyData.getInstance();
        List<AutoBuyData.BuyHistory> history = data.getHistory();
        
        int totalSpent = 0;
        int totalItems = history.size();
        
        for (AutoBuyData.BuyHistory entry : history) {
            totalSpent += entry.allPrice();
        }
        
        String text = String.format("""
            📊 *Статистика автобая*
            
            ✅ *Всего куплено:* %d предметов
            💰 *Потрачено:* %,d монет
            📈 *Средняя цена:* %,d монет
            💳 *Текущий баланс:* %,d монет
            🔢 *Множитель:* %.2f
            """,
            totalItems,
            totalSpent,
            totalItems > 0 ? totalSpent / totalItems : 0,
            data.getMoney(),
            data.getMultiplier()
        );
        
        sendTextWithKeyboard(chatId, text);
    }
    
    private void sendMainMenu(String chatId) {
        String text = """
            🎮 *Главное меню AutoBuy*
            
            Выберите действие с помощью кнопок ниже:
            """;
        
        sendTextWithKeyboard(chatId, text);
    }
    
    private void sendText(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    private void sendTextWithKeyboard(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");
        message.setReplyMarkup(createMainKeyboard());
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    private InlineKeyboardMarkup createMainKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("📊 Статус", "status"));
        row1.add(createButton("💰 Баланс", "balance"));
        keyboard.add(row1);
        
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("⏸️ Пауза", "pause"));
        row2.add(createButton("▶️ Продолжить", "resume"));
        keyboard.add(row2);
        
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("🏠 Хаб", "hub"));
        row3.add(createButton("💤 Перерыв", "break"));
        keyboard.add(row3);
        
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createButton("📈 Обновить цены", "getprices"));
        keyboard.add(row4);
        
        List<InlineKeyboardButton> row5 = new ArrayList<>();
        row5.add(createButton("📜 История", "history"));
        row5.add(createButton("📊 Статистика", "stats"));
        keyboard.add(row5);
        
        markup.setKeyboard(keyboard);
        return markup;
    }
    
    private InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
