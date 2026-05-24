package ru.cookiedlc.module.impl.misc.autobuy.holy.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.item.EnumItemType;
import ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.bot.AutoBuyBot;
import ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.manager.TelegramBotManager;

public class TelegramNotifier {
    
    public static void sendMessage(String text) {
        if (!TelegramBotManager.isRunning()) return;
        
        AutoBuyData data = AutoBuyData.getInstance();
        AutoBuyBot bot = TelegramBotManager.getBot();
        
        if (bot == null || data.getChatId().isEmpty()) return;
        
        SendMessage message = new SendMessage();
        message.setChatId(data.getChatId());
        message.setText(text);
        message.setParseMode("Markdown");
        
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки сообщения в Telegram: " + e.getMessage());
        }
    }
    
    public static void sendPurchaseNotification(String playerName, EnumItemType itemType, int price, 
                                                 int allPrice, int targetPrice, int balance) {
        if (!TelegramBotManager.isRunning()) return;
        
        double coefficient = (double) targetPrice / price;
        
        String message = String.format(
            "🔔 *Автобай купил предмет!*\n\n" +
            "👤 *Игрок:* %s\n" +
            "📦 *Предмет:* %s\n" +
            "💰 *Куплено за:* %,d монет\n" +
            "💵 *Цена за ед.:* %,d / %,d\n" +
            "📊 *Коэффициент окупа:* x%.2f\n" +
            "💳 *Баланс до покупки:* %,d монет\n" +
            "💳 *Баланс после:* %,d монет",
            playerName,
            itemType.getName(),
            allPrice,
            price,
            targetPrice,
            coefficient,
            balance,
            balance - allPrice
        );
        
        sendMessage(message);
    }
    
    public static void sendPricesUpdateNotification(int totalPrices, int foundPrices) {
        String message = String.format(
            "📈 *Цены обновлены!*\n\n" +
            "✅ *Найдено:* %d из %d предметов\n" +
            "⏱️ *Статус:* Готов к автобаю\n\n" +
            "Используйте кнопки ниже для управления.",
            foundPrices,
            totalPrices
        );
        
        sendMessage(message);
    }
    
    public static void sendStatusNotification(String status) {
        sendMessage("ℹ️ *Статус изменен*\n\n" + status);
    }
    
    public static void sendErrorNotification(String error) {
        sendMessage("❌ *Ошибка*\n\n" + error);
    }
}
