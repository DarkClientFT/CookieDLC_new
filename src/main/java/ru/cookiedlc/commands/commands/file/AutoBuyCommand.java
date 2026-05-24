package ru.cookiedlc.commands.commands.file;

import net.minecraft.util.Formatting;
import ru.cookiedlc.commands.api.command.Command;
import ru.cookiedlc.commands.api.command.argument.IArgConsumer;
import ru.cookiedlc.commands.api.command.exception.CommandException;
import ru.cookiedlc.commands.api.command.helpers.TabCompleteHelper;
import ru.cookiedlc.api.file.impl.AutoBuyFile;
import ru.cookiedlc.core.Main;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.manager.BuyingManager;
import ru.cookiedlc.module.impl.misc.autobuy.holy.manager.PricesManager;
import ru.cookiedlc.module.impl.misc.autobuy.holy.manager.SellManager;
import ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.manager.TelegramBotManager;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class AutoBuyCommand extends Command {
    
    private final Main main;
    
    public AutoBuyCommand(Main main) {
        super("autobuy");
        this.main = main;
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {
        String arg = args.hasAny() ? args.getString().toLowerCase(Locale.US) : "help";
        
        AutoBuyData data = AutoBuyData.getInstance();
        
        switch (arg) {
            case "buy" -> {
                if (data.getPriceForOne().isEmpty()) {
                    logDirect("Сначала получите цены командой: .autobuy prices", Formatting.RED);
                    return;
                }
                BuyingManager.start();
                logDirect("Автобай запущен!", Formatting.GREEN);
            }
            case "prices" -> {
                PricesManager.start();
                logDirect("Получение цен запущено...", Formatting.GREEN);
            }
            case "sell" -> {
                if (data.getPriceForOne().isEmpty()) {
                    logDirect("Сначала получите цены командой: .autobuy prices", Formatting.RED);
                    return;
                }
                SellManager.start();
                logDirect("Продажа запущена!", Formatting.GREEN);
            }
            case "stop" -> {
                BuyingManager.stop();
                SellManager.stop();
                PricesManager.setListen(false);
                data.setStatus("N/A");
                logDirect("Автобай остановлен!", Formatting.YELLOW);
            }
            case "save" -> {
                try {
                    AutoBuyFile file = new AutoBuyFile();
                    file.saveToFile(main.getClientInfoProvider().filesDir());
                    logDirect("Конфиг автобая сохранен!", Formatting.GREEN);
                } catch (Exception e) {
                    logDirect("Ошибка сохранения: " + e.getMessage(), Formatting.RED);
                }
            }
            case "load" -> {
                try {
                    AutoBuyFile file = new AutoBuyFile();
                    file.loadFromFile(main.getClientInfoProvider().filesDir());
                    logDirect("Конфиг автобая загружен!", Formatting.GREEN);
                    logDirect("Загружено " + data.getPriceForOne().size() + " цен", Formatting.GRAY);
                } catch (Exception e) {
                    logDirect("Ошибка загрузки: " + e.getMessage(), Formatting.RED);
                }
            }
            case "reset" -> {
                data.getPriceForOne().clear();
                data.getDisabledItems().clear();
                data.getStaffNicknames().clear();
                data.clearHistory();
                data.setItemsBought(0);
                data.setMultiplier(0.65);
                logDirect("Конфиг автобая сброшен!", Formatting.GREEN);
            }
            case "dir" -> {
                try {
                    File dir = main.getClientInfoProvider().filesDir();
                    if (!dir.exists()) dir.mkdirs();
                    Runtime.getRuntime().exec("explorer " + dir.getAbsolutePath());
                    logDirect("Открываю папку конфигов...", Formatting.GREEN);
                } catch (IOException e) {
                    logDirect("Не удалось открыть папку: " + e.getMessage(), Formatting.RED);
                }
            }
            case "status" -> {
                logDirect("=== Статус AutoBuy ===", Formatting.GOLD);
                logDirect("Статус: " + data.getStatus(), Formatting.WHITE);
                logDirect("Множитель: " + String.format("%.2f", data.getMultiplier()), Formatting.WHITE);
                logDirect("Загружено цен: " + data.getPriceForOne().size(), Formatting.WHITE);
                logDirect("Куплено предметов: " + data.getItemsBought(), Formatting.WHITE);
                logDirect("Баланс: " + data.getMoney(), Formatting.WHITE);
            }
            case "staff" -> {
                if (args.hasAny()) {
                    String subArg = args.getString().toLowerCase(Locale.US);
                    if (subArg.equals("add") && args.hasAny()) {
                        String nick = args.getString();
                        data.addStaff(nick);
                        logDirect("Добавлен стафф: " + nick, Formatting.GREEN);
                    } else if (subArg.equals("remove") && args.hasAny()) {
                        String nick = args.getString();
                        data.removeStaff(nick);
                        logDirect("Удален стафф: " + nick, Formatting.GREEN);
                    } else if (subArg.equals("list")) {
                        logDirect("Список стаффа: " + String.join(", ", data.getStaffNicknames()), Formatting.WHITE);
                    } else if (subArg.equals("clear")) {
                        data.getStaffNicknames().clear();
                        logDirect("Список стаффа очищен!", Formatting.GREEN);
                    }
                } else {
                    logDirect("Использование: .autobuy staff <add/remove/list/clear> [nick]", Formatting.GRAY);
                }
            }
            case "tg" -> handleTelegramCommand(args);
            default -> {
                logDirect("=== AutoBuy Команды ===", Formatting.GOLD);
                logDirect(".autobuy buy - Начать покупку", Formatting.WHITE);
                logDirect(".autobuy prices - Получить цены", Formatting.WHITE);
                logDirect(".autobuy sell - Продать предметы", Formatting.WHITE);
                logDirect(".autobuy stop - Остановить", Formatting.WHITE);
                logDirect(".autobuy save - Сохранить конфиг", Formatting.WHITE);
                logDirect(".autobuy load - Загрузить конфиг", Formatting.WHITE);
                logDirect(".autobuy reset - Сбросить конфиг", Formatting.WHITE);
                logDirect(".autobuy dir - Открыть папку конфигов", Formatting.WHITE);
                logDirect(".autobuy status - Показать статус", Formatting.WHITE);
                logDirect(".autobuy staff <add/remove/list/clear> - Управление стаффом", Formatting.WHITE);
                logDirect(".autobuy tg <add/start/stop/remove/status/test> - Telegram бот", Formatting.AQUA);
            }
        }
    }

    private void handleTelegramCommand(IArgConsumer args) throws CommandException {
        if (!args.hasAny()) {
            logDirect("=== Telegram Bot Команды ===", Formatting.GOLD);
            logDirect(".autobuy tg add <token> <chatId> - Добавить бота", Formatting.WHITE);
            logDirect(".autobuy tg start - Запустить бота", Formatting.WHITE);
            logDirect(".autobuy tg stop - Остановить бота", Formatting.WHITE);
            logDirect(".autobuy tg remove - Удалить бота", Formatting.WHITE);
            logDirect(".autobuy tg status - Статус бота", Formatting.WHITE);
            logDirect(".autobuy tg test - Отправить тестовое сообщение", Formatting.WHITE);
            return;
        }
        
        String subArg = args.getString().toLowerCase(Locale.US);
        TelegramBotManager manager = TelegramBotManager.getInstance();
        
        switch (subArg) {
            case "add" -> {
                if (args.has(2)) {
                    String token = args.getString();
                    String chatId = args.getString();
                    manager.addBot(token, chatId);
                    logDirect("Telegram бот добавлен!", Formatting.GREEN);
                    logDirect("Используйте .autobuy tg start для запуска", Formatting.GRAY);
                } else {
                    logDirect("Использование: .autobuy tg add <token> <chatId>", Formatting.RED);
                }
            }
            case "start" -> manager.startBot();
            case "stop" -> manager.stopBot();
            case "remove" -> manager.removeBot();
            case "status" -> {
                if (manager.isRunning()) {
                    logDirect("Telegram бот: " + Formatting.GREEN + "Запущен", Formatting.WHITE);
                } else if (manager.isConfigured()) {
                    logDirect("Telegram бот: " + Formatting.YELLOW + "Настроен, но не запущен", Formatting.WHITE);
                } else {
                    logDirect("Telegram бот: " + Formatting.RED + "Не настроен", Formatting.WHITE);
                }
            }
            case "test" -> {
                if (manager.isRunning()) {
                    ru.cookiedlc.module.impl.misc.autobuy.holy.telegram.TelegramNotifier.sendMessage(
                        "🧪 *Тестовое сообщение*\n\nБот работает корректно!"
                    );
                    logDirect("Тестовое сообщение отправлено!", Formatting.GREEN);
                } else {
                    logDirect("Сначала запустите бота: .autobuy tg start", Formatting.RED);
                }
            }
            default -> {
                logDirect("=== Telegram Bot Команды ===", Formatting.GOLD);
                logDirect(".autobuy tg add <token> <chatId> - Добавить бота", Formatting.WHITE);
                logDirect(".autobuy tg start - Запустить бота", Formatting.WHITE);
                logDirect(".autobuy tg stop - Остановить бота", Formatting.WHITE);
                logDirect(".autobuy tg remove - Удалить бота", Formatting.WHITE);
                logDirect(".autobuy tg status - Статус бота", Formatting.WHITE);
                logDirect(".autobuy tg test - Отправить тестовое сообщение", Formatting.WHITE);
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        if (args.hasAny() && args.hasExactlyOne()) {
            return new TabCompleteHelper()
                    .append("buy", "prices", "sell", "stop", "save", "load", "reset", "dir", "status", "staff", "tg")
                    .filterPrefix(args.getString())
                    .stream();
        } else if (args.hasAny()) {
            String arg = args.getString();
            if (arg.equalsIgnoreCase("staff") && args.hasExactlyOne()) {
                return new TabCompleteHelper()
                        .append("add", "remove", "list", "clear")
                        .filterPrefix(args.getString())
                        .stream();
            } else if (arg.equalsIgnoreCase("tg") && args.hasExactlyOne()) {
                return new TabCompleteHelper()
                        .append("add", "start", "stop", "remove", "status", "test")
                        .filterPrefix(args.getString())
                        .stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Управление автобаем";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "Команды для управления автобаем на аукционе",
                "",
                "Использование:",
                "> autobuy buy - Начать автоматическую покупку",
                "> autobuy prices - Получить рыночные цены",
                "> autobuy sell - Продать предметы из инвентаря",
                "> autobuy stop - Остановить все процессы",
                "> autobuy save - Сохранить конфиг (цены, настройки)",
                "> autobuy load - Загрузить конфиг",
                "> autobuy reset - Сбросить все настройки",
                "> autobuy dir - Открыть папку с конфигами",
                "> autobuy status - Показать текущий статус",
                "> autobuy staff <add/remove/list/clear> [nick] - Управление списком стаффа",
                "> autobuy tg add <token> <chatId> - Добавить Telegram бота",
                "> autobuy tg start - Запустить Telegram бота",
                "> autobuy tg stop - Остановить Telegram бота",
                "> autobuy tg remove - Удалить Telegram бота",
                "> autobuy tg status - Статус Telegram бота",
                "> autobuy tg test - Отправить тестовое сообщение"
        );
    }
}
