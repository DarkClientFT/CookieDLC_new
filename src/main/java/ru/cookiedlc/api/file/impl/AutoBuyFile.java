package ru.cookiedlc.api.file.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.cookiedlc.api.file.ClientFile;
import ru.cookiedlc.api.file.exception.FileLoadException;
import ru.cookiedlc.api.file.exception.FileSaveException;
import ru.cookiedlc.module.impl.misc.autobuy.holy.data.AutoBuyData;
import ru.cookiedlc.module.impl.misc.autobuy.holy.item.EnumItemType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AutoBuyFile extends ClientFile {
    
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AutoBuyFile() {
        super("autobuy");
    }

    @Override
    public void saveToFile(File path) throws FileSaveException {
        File file = new File(path, getName() + ".json");
        AutoBuyData data = AutoBuyData.getInstance();
        
        AutoBuyConfig config = new AutoBuyConfig();
        config.multiplier = data.getMultiplier();
        config.refreshDelay = data.getRefreshDelay();
        config.disabledItems = new HashSet<>();
        config.staffNicknames = new HashSet<>(data.getStaffNicknames());
        config.prices = new HashMap<>();
        config.botToken = data.getBotToken();
        config.chatId = data.getChatId();
        config.botEnabled = data.isBotEnabled();
        
        for (EnumItemType item : data.getDisabledItems()) {
            config.disabledItems.add(item.name());
        }
        
        for (var entry : data.getPriceForOne().entrySet()) {
            config.prices.put(entry.getKey().name(), entry.getValue());
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(config, writer);
        } catch (JsonIOException | IOException e) {
            throw new FileSaveException(String.format("Failed to save %s to file", getName()), e);
        }
    }

    @Override
    public void loadFromFile(File path) throws FileLoadException {
        File file = new File(path, getName() + ".json");
        
        if (!file.exists()) {
            return;
        }
        
        AutoBuyData data = AutoBuyData.getInstance();

        try (FileReader reader = new FileReader(file)) {
            AutoBuyConfig config = gson.fromJson(reader, AutoBuyConfig.class);
            
            if (config == null) return;
            
            data.setMultiplier(config.multiplier);
            data.setRefreshDelay(config.refreshDelay);
            data.setBotToken(config.botToken != null ? config.botToken : "");
            data.setChatId(config.chatId != null ? config.chatId : "");
            data.setBotEnabled(config.botEnabled);
            
            data.getDisabledItems().clear();
            if (config.disabledItems != null) {
                for (String itemName : config.disabledItems) {
                    try {
                        EnumItemType item = EnumItemType.valueOf(itemName);
                        data.getDisabledItems().add(item);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            
            data.getStaffNicknames().clear();
            if (config.staffNicknames != null) {
                data.getStaffNicknames().addAll(config.staffNicknames);
            }
            
            data.getPriceForOne().clear();
            if (config.prices != null) {
                for (var entry : config.prices.entrySet()) {
                    try {
                        EnumItemType item = EnumItemType.valueOf(entry.getKey());
                        data.getPriceForOne().put(item, entry.getValue());
                    } catch (IllegalArgumentException ignored) {}
                }
            }
            
        } catch (IOException e) {
            throw new FileLoadException(String.format("Failed to load %s from file", getName()), e);
        } catch (JsonSyntaxException e) {
            throw new FileLoadException(String.format("JSON syntax error, %s config cannot be loaded", getName()), e);
        } catch (JsonIOException e) {
            throw new FileLoadException(String.format("JSON IO error, %s config cannot be loaded", getName()), e);
        }
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AutoBuyConfig {
        double multiplier = 0.65;
        int refreshDelay = 30;
        HashSet<String> disabledItems = new HashSet<>();
        HashSet<String> staffNicknames = new HashSet<>();
        HashMap<String, Integer> prices = new HashMap<>();
        String botToken = "";
        String chatId = "";
        boolean botEnabled = false;
    }
}
