package ru.cookiedlc.api.auth.protect.loader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.cookiedlc.common.util.logger.LoggerUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

public class Loader {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static boolean validated = false;
    private static String sessionUsername = null;
    private static String ircToken = null;
    private static String prefixDisplay = null;
    private static String prefix = null;
    private static boolean isAdmin = false;

    public static void validateOrlllllllllllllllllllllllllllllllllllllllllllllllllll() {
        LoggerUtil.info("[CookieDLC] Validating loader session...");
        String sessionToken = System.getProperty("cookiedlc.session");
        if (sessionToken == null || sessionToken.isEmpty()) {
            return;
        }
        LoggerUtil.info("[CookieDLC] Session token found: " +
                sessionToken.substring(0, Math.min(16, sessionToken.length())) + "...");
        String hwid = generateHWID();
        LoggerUtil.info("[CookieDLC] HWID: " + hwid.substring(0, 12) + "...");
        try {
            JsonObject body = new JsonObject();
            body.addProperty("session_token", sessionToken);
            body.addProperty("hwid", hwid);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http:
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            LoggerUtil.info("[CookieDLC] Validation response: " + response.statusCode());

            if (response.statusCode() != 200) {
                return;
            }

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

            boolean valid = json.has("valid") && json.get("valid").getAsBoolean();

            if (!valid) {
                String msg = json.has("message")
                        ? json.get("message").getAsString()
                        : "Unknown validation error";
                return;
            }

            validated = true;
            sessionUsername = json.has("username")
                    ? json.get("username").getAsString() : "Player";
            ircToken = json.has("irc_token")
                    ? json.get("irc_token").getAsString() : null;
            prefix = json.has("prefix")
                    ? json.get("prefix").getAsString() : "USER";
            prefixDisplay = json.has("prefix_display")
                    ? json.get("prefix_display").getAsString() : "USER";
            isAdmin = json.has("is_admin")
                    && json.get("is_admin").getAsBoolean();

            LoggerUtil.info("[CookieDLC] Session VALIDATED! " +
                    "Welcome, " + sessionUsername +
                    " | Prefix: " + prefix +
                    " | Admin: " + isAdmin);

        } catch (Exception e) {
            LoggerUtil.error("[CookieDLC] Validation error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String generateHWID() {
        try {
            String userName = System.getProperty("user.name", "");
            String procId = System.getenv("PROCESSOR_IDENTIFIER");
            if (procId == null) procId = "";
            String compName = System.getenv("COMPUTERNAME");
            if (compName == null) compName = "";
            String userHome = System.getProperty("user.home", "");
            String toHash = "Windows 10"
                    + "amd64"
                    + userName
                    + procId
                    + compName
                    + userHome;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 32);

        } catch (Exception e) {
            LoggerUtil.error("[CookieDLC] HWID generation error: " + e.getMessage());
            return "unknown-hwid";
        }
    }
    public static void ensureValidated() {
        if (!validated) {
        }
    }

    public static boolean isValidated() {
        return validated;
    }

    public static String getUsername() {
        return sessionUsername;
    }

    public static String getIrcToken() {
        return ircToken;
    }

    public static String getPrefix() {
        return prefix;
    }

    public static String getPrefixDisplay() {
        return prefixDisplay;
    }

    public static boolean isAdmin() {
        return isAdmin;
    }

}