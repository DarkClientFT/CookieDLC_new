package ru.cookiedlc.core.client;

import ru.cookiedlc.api.auth.AuthData;
import ru.cookiedlc.api.auth.protect.CookieProtect;
import ru.cookiedlc.common.util.other.StringUtil;

import java.io.File;
@CookieProtect
public record ClientInfo(String clientName, String userName, String role, File clientDir, File filesDir, File configsDir) implements ClientInfoProvider {

    @Override
    public String getFullInfo() {
        return String.format("Welcome! Client: %s Version: %s Branch: %s", clientName, AuthData.getInstance().getUsername(), StringUtil.getUserRole());
    }
}