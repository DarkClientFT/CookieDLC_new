package ru.cookiedlc.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import ru.cookiedlc.api.auth.AuthData;
import ru.cookiedlc.api.auth.AuthManager;
import ru.cookiedlc.api.auth.protect.loader.Loader;
import ru.cookiedlc.api.auth.protect.CookieProtect;
import ru.cookiedlc.api.file.exception.FileProcessingException;
import ru.cookiedlc.api.repository.box.BoxESPRepository;
import ru.cookiedlc.api.repository.rct.RCTRepository;
import ru.cookiedlc.api.repository.way.WayRepository;
import ru.cookiedlc.api.system.discord.DiscordManager;
import ru.cookiedlc.ui.clickgui.MenuScreen;
import ru.cookiedlc.ui.dropdowngui.DropDownScreen;
import ru.cookiedlc.ui.hud.api.DraggableRepository;
import ru.cookiedlc.api.file.*;
import ru.cookiedlc.api.repository.macro.MacroRepository;
import ru.cookiedlc.event.api.EventManager;
import ru.cookiedlc.module.api.ModuleProvider;
import ru.cookiedlc.module.api.ModuleRepository;
import ru.cookiedlc.module.api.ModuleSwitcher;
import ru.cookiedlc.api.system.sound.SoundManager;
import ru.cookiedlc.common.util.logger.LoggerUtil;
import ru.cookiedlc.common.util.render.ScissorManager;
import ru.cookiedlc.core.client.ClientInfo;
import ru.cookiedlc.core.client.ClientInfoProvider;
import ru.cookiedlc.core.listener.ListenerRepository;
import ru.cookiedlc.commands.CommandDispatcher;
import ru.cookiedlc.commands.api.manager.CommandRepository;
import ru.cookiedlc.module.impl.combat.killaura.attack.AttackPerpetrator;

import java.io.File;
@CookieProtect
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Main implements ModInitializer {
 ⠀⠀      ⣠⣶⣿⣿⣿⣷⣤⡀⠀⠀⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⢀⣾⡿⠋⠀⠿⠇⠉⠻⣿⣄⠀⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⢠⣿⠏⠀⠀⠀⠀⠀⠀⠀⠙⣿⣆⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⢠⣿⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⣿⣆⠀⠀⠀⠀
 ⠀⠀⠀⠀⢸⣿⡄⠀⠀⠀⢀⣤⣀⠀⠀⠀⠀⣿⡿⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠻⣿⣶⣶⣾⡿⠟⢿⣷⣶⣶⣿⡟⠁⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡏⠉⠁⠀⠀⠀⠀⠉⠉⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⠀⠀⣸⣿⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⠀⠀⠀⣿⡇⢀⣴⣿⠇⠀⠀⠀⠀⣿⡇⠀⠀⠀⠀⠀
 ⠀⠀⠀⢀⣠⣴⣿⣷⣿⠟⠁⠀⠀⠀⠀ ⠀⣿⣧⣄⡀⠀⠀⠀
 ⠀⢀⣴⡿⠛⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀ ⠈⠉⠙⢿⣷⣄⠀
 ⢠⣿⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀   ⠙⣿⣆
 ⣿⡟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀    ⠀⢹⣿
 ⣿⣇⠀⠀⠀⠀⠀⠀⢸⣿⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀   ⢸⣿
 ⢹⣿⡄⠀⠀⠀⠀⠀⠀⢿⣷⠀⠀⠀⠀⠀⠀⠀⠀  ⠀⢀⣾⡿
 ⠀⠻⣿⣦⣀⠀⠀⠀⠀⠈⣿⣷⣄⡀⠀⠀⠀⠀⣀⣤⣾⡟⠁
 ⠀⠀⠈⠛⠿⣿⣷⣶⣾⡿⠿⠛⠻⢿⣿⣶⣾⣿⠿⠛/*/

    @Getter
    static Main instance;

    AuthManager authManager;
    EventManager eventManager = new EventManager();
    ModuleRepository moduleRepository;
    ModuleSwitcher moduleSwitcher;
    CommandRepository commandRepository;
    CommandDispatcher commandDispatcher;
    BoxESPRepository boxESPRepository;
    MacroRepository macroRepository;
    WayRepository wayRepository;
    RCTRepository RCTRepository;
    ModuleProvider moduleProvider;
    DraggableRepository draggableRepository;
    DiscordManager discordManager;
    FileRepository fileRepository;
    FileController fileController;
    ScissorManager scissorManager = new ScissorManager();
    ClientInfoProvider clientInfoProvider;
    ListenerRepository listenerRepository;
    AttackPerpetrator attackPerpetrator = new AttackPerpetrator();
    boolean initialized;

    @Override
    public void onInitialize() {
        instance = this;
        Loader.validateOrlllllllllllllllllllllllllllllllllllllllllllllllllll();
        LoggerUtil.info("=== CookieDLC Session Validated ===");
        authManager = AuthManager.getInstance();
        authManager.autoAuthOrlllllllllllllllllllllllllllllllll();
        LoggerUtil.info("=== CookieDLC Auth Complete: " +
                AuthData.getInstance().getUsername() + " ===");
        boxESPRepository = new BoxESPRepository(eventManager);
        macroRepository = new MacroRepository(eventManager);
        wayRepository = new WayRepository(eventManager);
        RCTRepository = new RCTRepository(eventManager);
        initClientInfoProvider();
        initModules();
        initDraggable();
        initFileManager();
        initCommands();
        initListeners();
        initDiscordRPC();
        SoundManager.init();
        MenuScreen menuScreen = new MenuScreen();
        menuScreen.initialize();
        DropDownScreen dropDownScreen = new DropDownScreen();
        dropDownScreen.initialize();

        initialized = true;
        LoggerUtil.info("=== CookieDLC Fully Initialized! ===");
    }

    private void initDraggable() {
        draggableRepository = new DraggableRepository();
        draggableRepository.setup();
    }

    private void initModules() {
        Loader.ensureValidated();
        moduleRepository = new ModuleRepository();
        moduleRepository.setup();
        moduleProvider = new ModuleProvider(moduleRepository.modules());
        moduleSwitcher = new ModuleSwitcher(moduleRepository.modules(), eventManager);
    }

    private void initCommands() {
        commandRepository = new CommandRepository();
        commandDispatcher = new CommandDispatcher(eventManager);
    }

    private void initDiscordRPC() {
        discordManager = new DiscordManager();
        discordManager.init();
    }

    private void initClientInfoProvider() {
        String username = AuthData.getInstance().getUsername();
        if (username == null || username.isEmpty()) {
            username = Loader.getUsername();
        }
        if (username == null || username.isEmpty()) {
            username = "Player";
        }

        String prefix = Loader.getPrefixDisplay();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "USER";
        }

        File clientDirectory = new File(
                MinecraftClient.getInstance().runDirectory, "cookiedlc");
        File filesDirectory = new File(clientDirectory, "files");
        File moduleFilesDirectory = new File(filesDirectory, "config");

        clientInfoProvider = new ClientInfo(
                "CookieDLC",
                username,
                prefix,
                clientDirectory,
                filesDirectory,
                moduleFilesDirectory
        );
    }

    private void initFileManager() {
        DirectoryCreator directoryCreator = new DirectoryCreator();
        directoryCreator.createDirectories(
                clientInfoProvider.clientDir(),
                clientInfoProvider.filesDir(),
                clientInfoProvider.configsDir()
        );
        fileRepository = new FileRepository();
        fileRepository.setup(this);
        fileController = new FileController(
                fileRepository.getClientFiles(),
                clientInfoProvider.filesDir(),
                clientInfoProvider.configsDir()
        );
        try {
            fileController.loadFiles();
        } catch (FileProcessingException e) {
            LoggerUtil.error("Error loading files: "
                    + e.getMessage() + " " + e.getCause());
        }
    }

    private void initListeners() {
        listenerRepository = new ListenerRepository();
        listenerRepository.setup();
    }
}