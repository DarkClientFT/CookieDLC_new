package ru.cookiedlc.commands.commands.other;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Formatting;

import ru.cookiedlc.api.auth.AuthData;
import ru.cookiedlc.api.auth.AuthManager;
import ru.cookiedlc.commands.api.command.Command;
import ru.cookiedlc.commands.api.command.argument.IArgConsumer;
import ru.cookiedlc.commands.api.command.exception.CommandException;
import ru.cookiedlc.common.QuickImports;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class IRCCommand extends Command implements QuickImports {

    private static long lastMessageTime = 0;

    private static final long COOLDOWN_MS = 1000;

    public IRCCommand() {
        super("irc");
    }

    @Override
    public void execute(String label, IArgConsumer args) throws CommandException {

        long currentTime = System.currentTimeMillis();
        long timeSinceLastMessage = currentTime - lastMessageTime;

        if (timeSinceLastMessage < COOLDOWN_MS) {
            long remainingTime = COOLDOWN_MS - timeSinceLastMessage;
            double remainingSeconds = remainingTime / 1000.0;
            logDirect(String.format("[IRC] Подождите %.1f сек.", remainingSeconds), Formatting.YELLOW);
            return;
        }

        if (!args.hasAny()) {
            logDirect("[IRC] Использование: .irc <сообщение>", Formatting.RED);
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        while (args.hasAny()) {
            if (messageBuilder.length() > 0) {
                messageBuilder.append(" ");
            }
            messageBuilder.append(args.getString());
        }
        String message = messageBuilder.toString().trim();

        if (message.isEmpty()) {
            logDirect("[IRC] Введите сообщение!", Formatting.RED);
            return;
        }

        boolean isAuthorized = AuthData.getInstance().isAuthorized();

        if (!isAuthorized) {
            logDirect("[IRC] Вы не авторизованы! Активируйте ключ в главном меню.", Formatting.RED);
            return;
        }

        var wsClient = AuthManager.getInstance().getWsClient();
        if (wsClient == null || !wsClient.isConnected()) {
            logDirect("[IRC] Нет подключения к IRC серверу!", Formatting.RED);

            String token = AuthData.getInstance().getToken();
            if (token != null) {
                logDirect("[IRC] Попытка переподключения...", Formatting.YELLOW);
                AuthManager.getInstance().reconnectWebSocket(token);
            }
            return;
        }

        AuthManager.getInstance().sendIRCMessage(message);
        lastMessageTime = System.currentTimeMillis();
    }

    @Override
    public Stream<String> tabComplete(String label, IArgConsumer args) throws CommandException {
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Отправить сообщение в IRC чат";
    }

    
    @Override
    public List<String> getLongDesc() {
        return Arrays.asList(
                "IRC чат для общения между пользователями клиента",
                "",
                "Использование:",
                "> irc <сообщение> - отправить сообщение всем онлайн",
                "",
                "Задержка между сообщениями: 1 секунда"
        );
    }
}