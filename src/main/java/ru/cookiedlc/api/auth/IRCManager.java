package ru.cookiedlc.api.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IRCManager extends org.java_websocket.client.WebSocketClient {

    private final ScheduledExecutorService pingExecutor = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean connected = false;
    private static final Map<Character, Formatting> color = new HashMap<>();
    static {
        color.put('0', Formatting.BLACK);
        color.put('1', Formatting.DARK_BLUE);
        color.put('2', Formatting.DARK_GREEN);
        color.put('3', Formatting.DARK_AQUA);
        color.put('4', Formatting.DARK_RED);
        color.put('5', Formatting.DARK_PURPLE);
        color.put('6', Formatting.GOLD);
        color.put('7', Formatting.GRAY);
        color.put('8', Formatting.DARK_GRAY);
        color.put('9', Formatting.BLUE);
        color.put('a', Formatting.GREEN);
        color.put('b', Formatting.AQUA);
        color.put('c', Formatting.RED);
        color.put('d', Formatting.LIGHT_PURPLE);
        color.put('e', Formatting.YELLOW);
        color.put('f', Formatting.WHITE);
        color.put('k', Formatting.OBFUSCATED);
        color.put('l', Formatting.BOLD);
        color.put('m', Formatting.STRIKETHROUGH);
        color.put('n', Formatting.UNDERLINE);
        color.put('o', Formatting.ITALIC);
        color.put('r', Formatting.RESET);
    }

    public IRCManager(String serverUri) throws Exception {
        super(new URI(serverUri));
        this.setConnectionLostTimeout(60);

    }

    private MinecraftClient getMc() {
        try {
            return MinecraftClient.getInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private Text parseColoredText(String input) {
        if (input == null || input.isEmpty()) {
            return Text.literal("");
        }

        input = input.replace('&', '§');

        MutableText result = Text.empty();
        StringBuilder currentText = new StringBuilder();
        Formatting[] currentFormats = new Formatting[0];

        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);

            if (c == '§' && i + 1 < input.length()) {

                if (currentText.length() > 0) {
                    MutableText part = Text.literal(currentText.toString());
                    for (Formatting f : currentFormats) {
                        part = part.formatted(f);
                    }
                    result.append(part);
                    currentText = new StringBuilder();
                }

                char code = Character.toLowerCase(input.charAt(i + 1));
                Formatting format = color.get(code);

                if (format != null) {
                    if (format == Formatting.RESET) {
                        currentFormats = new Formatting[0];
                    } else if (format.isColor()) {
                        currentFormats = new Formatting[]{format};
                    } else {

                        Formatting[] newFormats = new Formatting[currentFormats.length + 1];
                        System.arraycopy(currentFormats, 0, newFormats, 0, currentFormats.length);
                        newFormats[currentFormats.length] = format;
                        currentFormats = newFormats;
                    }
                    i += 2;
                    continue;
                }
            }

            currentText.append(c);
            i++;
        }

        if (currentText.length() > 0) {
            MutableText part = Text.literal(currentText.toString());
            for (Formatting f : currentFormats) {
                part = part.formatted(f);
            }
            result.append(part);
        }

        return result;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        connected = true;

        pingExecutor.scheduleAtFixedRate(() -> {
            if (isOpen()) {
                JsonObject ping = new JsonObject();
                ping.addProperty("type", "ping");
                send(ping.toString());
            }
        }, 30, 30, TimeUnit.SECONDS);

        MinecraftClient mc = getMc();
        if (mc != null) {
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.sendMessage(
                            Text.literal(Formatting.GREEN + "[IRC] " + Formatting.RESET + "Подключено к IRC чату"),
                            false
                    );
                }
            });
        }
    }

    @Override
    public void onMessage(String message) {


        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();

            if (type.equals("irc")) {
                String sender = json.get("sender").getAsString();
                String prefix = json.has("prefix") ? json.get("prefix").getAsString() : "§7USER";
                String text = json.get("message").getAsString();
                MinecraftClient mc = getMc();
                if (mc != null) {
                    mc.execute(() -> {
                        if (mc.player != null) {
                            MutableText fullMessage = Text.empty();
                            fullMessage.append(Text.literal("§l[IRC] ").formatted(Formatting.AQUA));
                            if (sender.equals("SYSTEM")) {
                                fullMessage.append(Text.literal(text).formatted(Formatting.GRAY));
                            } else {
                                fullMessage.append(Text.literal(""));
                                fullMessage.append(parseColoredText(prefix));
                                fullMessage.append(Text.literal(" ").formatted(Formatting.RESET));
                                fullMessage.append(Text.literal(sender).formatted(Formatting.WHITE));
                                fullMessage.append(Text.literal(" » ").formatted(Formatting.GRAY));
                                fullMessage.append(Text.literal(text).formatted(Formatting.WHITE));
                            }
                            mc.player.sendMessage(fullMessage, false);
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("[WS] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        connected = false;

        try {
            pingExecutor.shutdownNow();
        } catch (Exception ignored) {}

        MinecraftClient mc = getMc();
        if (mc != null) {
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.sendMessage(
                            Text.literal(Formatting.RED + "[IRC] " + Formatting.RESET + "Отключено от IRC чата"),
                            false
                    );
                }
            });
        }
    }

    @Override
    public void onError(Exception ex) {

        ex.printStackTrace();
    }

    @Override
    public void send(String text) {

        super.send(text);
    }

    public boolean isConnected() {
        return connected && isOpen();
    }
}