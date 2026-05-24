package ru.cookiedlc.api.repository.account;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import ru.cookiedlc.core.Main;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AccountManager {

    private static AccountManager instance;

    @Getter
    private final List<String> accounts = new ArrayList<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File accountsFile;

    @Getter
    private String lastSelectedAccount = "";

    public AccountManager() {
        this.accountsFile = new File(Main.getInstance().getClientInfoProvider().clientDir(), "accounts.json");
        load();
    }

    public static AccountManager getInstance() {
        if (instance == null) {
            instance = new AccountManager();
        }
        return instance;
    }

    public void addAccount(String name) {
        if (!accounts.contains(name)) {
            accounts.add(name);
            save();
        }
    }

    public void removeAccount(String name) {
        accounts.remove(name);
        save();
    }

    public void clearAll() {
        accounts.clear();
        lastSelectedAccount = "";
        save();
    }

    public void setLastSelectedAccount(String name) {
        this.lastSelectedAccount = name;
        save();
    }

    public void load() {
        if (!accountsFile.exists()) {
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(accountsFile), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<AccountData>() {}.getType();
            AccountData data = gson.fromJson(reader, type);

            if (data != null) {
                accounts.clear();
                if (data.accounts != null) {
                    accounts.addAll(data.accounts);
                }
                lastSelectedAccount = data.lastSelected != null ? data.lastSelected : "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            if (!accountsFile.getParentFile().exists()) {
                accountsFile.getParentFile().mkdirs();
            }

            AccountData data = new AccountData();
            data.accounts = new ArrayList<>(accounts);
            data.lastSelected = lastSelectedAccount;

            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(accountsFile), StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class AccountData {
        List<String> accounts;
        String lastSelected;
    }
}