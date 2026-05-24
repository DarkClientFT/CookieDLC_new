package ru.cookiedlc.api.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import ru.cookiedlc.api.auth.protect.loader.Loader;
import ru.cookiedlc.common.util.logger.LoggerUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.CompletableFuture;

public class AuthManager {
    private static AuthManager instance;
    private IRCManager wsClient;

    private MinecraftClient getMc() {
        try {
            return MinecraftClient.getInstance();
        } catch (Exception e) {
            return null;
        }
    }

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }


    public void autoAuthOrlllllllllllllllllllllllllllllllll() {
        LoggerUtil.info("[Auth] Starting auto-authentication...");
        if (Loader.isValidated()) {
            String username = Loader.getUsername();
            String ircToken = Loader.getIrcToken();
            String prefix = Loader.getPrefixDisplay();
            if (username != null && ircToken != null) {
                LoggerUtil.info("[Auth] Using session data from loader validator");
                AuthData.getInstance().setAuthorized(true, username, ircToken);
                connectWebSocket(ircToken);

                LoggerUtil.info("[Auth] Auto-auth complete! User: " + username);
                return;
            }
        }
        LoggerUtil.info("[Auth] Fallback: reading license key from loader file...");
        String licenseKey = loadLoaderLicenseKey();
        if (licenseKey == null || licenseKey.isEmpty()) {
            LoggerUtil.error("[Auth] No license key found at: " + "C:\\CookieDLC\\license.key");
            return;
        }

        LoggerUtil.info("[Auth] License key found: " +
                licenseKey.substring(0, Math.min(8, licenseKey.length())) + "...");

        try {
            AuthResult result = authenticateSync(licenseKey);

            if (result.success) {
                LoggerUtil.info("[Auth] Auto-auth SUCCESS! Welcome, " + result.message);

            } else if (result.banned) {
            } else {
            }
        } catch (Exception e) {
            LoggerUtil.error("[Auth] Auto-auth exception: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private String loadLoaderLicenseKey() {
        try {
            File loaderKeyFile = new File("C:\\CookieDLC\\license.key");

            if (loaderKeyFile.exists() && loaderKeyFile.isFile()) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(loaderKeyFile), StandardCharsets.UTF_8))) {
                    String key = reader.readLine();
                    if (key != null) {
                        key = key.trim();
                        if (!key.isEmpty()) {
                            LoggerUtil.info("[Auth] Loaded key from loader: " + "C:\\CookieDLC\\license.key");
                            return key;
                        }
                    }
                }
            } else {
                LoggerUtil.info("[Auth] Loader key file not found: " + "C:\\CookieDLC\\license.key");
            }
        } catch (Exception e) {
            LoggerUtil.error("[Auth] Error reading loader key: " + e.getMessage());
        }

        return loadSavedKey();
    }

    private AuthResult authenticateSync(String licenseKey) {
        try {
            URL url = new URL("http:
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setDoOutput(true);

            JsonObject body = new JsonObject();
            body.addProperty("license_key", licenseKey);
            body.addProperty("hwid", getHWID());

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();

            InputStream is = responseCode >= 400
                    ? conn.getErrorStream()
                    : conn.getInputStream();

            if (is == null) {
                return new AuthResult(false, false, "Server not responding");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
            boolean success = json.get("success").getAsBoolean();

            if (success) {
                String username = json.get("username").getAsString();
                String token = json.get("token").getAsString();

                AuthData.getInstance().setAuthorized(true, username, token);
                connectWebSocket(token);
                saveLicenseKey(licenseKey);

                return new AuthResult(true, false, username);
            } else {
                boolean banned = json.has("banned") && json.get("banned").getAsBoolean();
                String message = json.has("message")
                        ? json.get("message").getAsString()
                        : "Unknown error";

                return new AuthResult(false, banned, message);
            }

        } catch (java.net.ConnectException e) {
            return new AuthResult(false, false,
                    "Cannot connect to auth server");
        } catch (java.net.SocketTimeoutException e) {
            return new AuthResult(false, false,
                    "Connection timeout (try VPN)");
        } catch (Exception e) {
            e.printStackTrace();
            return new AuthResult(false, false, e.getMessage());
        }
    }


    public String getHWID() {
        try {
            String toHash = System.getProperty("os.name") +
                    System.getProperty("os.arch") +
                    System.getProperty("user.name") +
                    System.getenv("PROCESSOR_IDENTIFIER") +
                    System.getenv("COMPUTERNAME") +
                    System.getProperty("user.home");

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(toHash.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 32);
        } catch (Exception e) {
            return "unknown-hwid-" + System.currentTimeMillis();
        }
    }


    public CompletableFuture<AuthResult> authenticate(String licenseKey) {
        return CompletableFuture.supplyAsync(() -> authenticateSync(licenseKey));
    }


    private void connectWebSocket(String token) {
        try {
            if (wsClient != null) {
                try {
                    wsClient.close();
                } catch (Exception ignored) {}
            }

            String wsUrl = "http:
                    .replace("https:
            String fullUrl = wsUrl + "/ws/" + token;

            LoggerUtil.info("[Auth] Connecting WebSocket: " +
                    fullUrl.substring(0, Math.min(50, fullUrl.length())) + "...");

            wsClient = new IRCManager(fullUrl);
            wsClient.connect();

            LoggerUtil.info("[Auth] WebSocket connected!");
        } catch (Exception e) {
            LoggerUtil.error("[Auth] WebSocket connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void reconnectWebSocket(String token) {
        CompletableFuture.runAsync(() -> {
            try {
                if (wsClient != null) {
                    try {
                        wsClient.close();
                    } catch (Exception ignored) {}
                    wsClient = null;
                }

                Thread.sleep(500);

                String wsUrl = "http:
                        .replace("https:
                String fullUrl = wsUrl + "/ws/" + token;

                wsClient = new IRCManager(fullUrl);
                wsClient.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void sendIRCMessage(String message) {
        if (wsClient != null && wsClient.isConnected()) {
            JsonObject data = new JsonObject();
            data.addProperty("type", "irc");
            data.addProperty("message", message);
            wsClient.send(data.toString());
        }
    }

    public void disconnect() {
        if (wsClient != null) {
            try {
                wsClient.close();
            } catch (Exception ignored) {}
            wsClient = null;
        }
        AuthData.getInstance().reset();
    }


    private void saveLicenseKey(String key) {
        try {
            File loaderDir = new File("C:\\CookieDLC");
            if (!loaderDir.exists()) loaderDir.mkdirs();

            File file = new File("C:\\CookieDLC\\license.key");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(key);
            }
            LoggerUtil.info("[Auth] License key saved to: " + "C:\\CookieDLC\\license.key");
        } catch (Exception e) {
            LoggerUtil.error("[Auth] Failed to save key: " + e.getMessage());
        }
        try {
            MinecraftClient mc = getMc();
            File dir;
            if (mc != null && mc.runDirectory != null) {
                dir = new File(mc.runDirectory, "cookiedlc");
            } else {
                dir = new File(System.getProperty("user.home"), ".cookiedlc");
            }
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "license.key");
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(key);
            }
        } catch (Exception e) {
        }
    }

    public String loadSavedKey() {
        try {
            File loaderKey = new File("C:\\CookieDLC\\license.key");
            if (loaderKey.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(loaderKey))) {
                    String key = reader.readLine();
                    if (key != null && !key.trim().isEmpty()) {
                        return key.trim();
                    }
                }
            }
        } catch (Exception e) {
        }
        try {
            MinecraftClient mc = getMc();
            File file = null;

            if (mc != null && mc.runDirectory != null) {
                file = new File(mc.runDirectory, "cookiedlc/license.key");
            }

            if (file == null || !file.exists()) {
                file = new File(System.getProperty("user.home"), ".cookiedlc/license.key");
            }

            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String key = reader.readLine();
                    return key != null ? key.trim() : null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public IRCManager getWsClient() {
        return wsClient;
    }




    public static class AuthResult {
        public final boolean success;
        public final boolean banned;
        public final String message;

        public AuthResult(boolean success, boolean banned, String message) {
            this.success = success;
            this.banned = banned;
            this.message = message;
        }
    }
}