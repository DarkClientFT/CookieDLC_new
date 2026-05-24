package ru.cookiedlc.api.auth;

public class AuthData {
    private static AuthData instance;
    private String username = "null";
    private boolean authorized = false;
    private String token = null;

    public static AuthData getInstance() {
        if (instance == null) {
            instance = new AuthData();
        }
        return instance;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized, String username, String token) {
        this.authorized = authorized;
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public void reset() {
        this.authorized = false;
        this.username = null;
        this.token = null;
    }
}