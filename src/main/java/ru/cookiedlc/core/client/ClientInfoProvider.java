package ru.cookiedlc.core.client;

import ru.cookiedlc.api.auth.protect.CookieProtect;

import java.io.File;
@CookieProtect
public interface ClientInfoProvider {
    String userName();
    String clientName();
    String role();

    String getFullInfo();

    File clientDir();

    File filesDir();

    File configsDir();
}