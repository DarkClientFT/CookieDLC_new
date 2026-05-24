package ru.cookiedlc.module.impl.misc;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import ru.cookiedlc.event.api.EventHandler;
import ru.cookiedlc.module.api.Module;
import ru.cookiedlc.module.api.ModuleCategory;
import ru.cookiedlc.event.events.packet.PacketEvent;
import ru.cookiedlc.event.events.player.TickEvent;

import java.util.*;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Plugins extends Module {
    
    @NonFinal
    int delay = 0;
    
    String[] knownPlugins = {
            "matrix", "alice", "vulcan", "kauri", "spartan", "polar", "horizon",
            "intave", "prostoac", "tesla", "buzz", "grimac", "grim", "aac",
            "nocheatplus", "anticheatreloaded", "negativity", "cheatminecore",
            "cmcore", "themis", "sloth"
    };

    public Plugins() {
        super("Plugins", "Plugins", ModuleCategory.MISC);
    }

    @Override
    public void activate() {
        delay = 0;
        if (mc.player == null || mc.player.networkHandler == null) {
            setState(false);
            return;
        }
        mc.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));
    }

    @Override
    public void deactivate() {
        delay = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) return;
        
        delay++;
        if (delay > 40) {
            logDirect("Не удалось получить список плагинов.", Formatting.RED);
            delay = 0;
            setState(false);
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (event.getType() != PacketEvent.Type.RECEIVE) return;
        
        if (event.getPacket() instanceof CommandSuggestionsS2CPacket packet) {
            Set<String> plugins = packet.getSuggestions().getList().stream()
                    .map(cmd -> {
                        String[] command = cmd.getText().split(":");
                        if (command.length > 1) {
                            return command[0].replace("/", "");
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (!plugins.isEmpty()) {
                List<String> knownList = Arrays.stream(knownPlugins).map(String::toLowerCase).toList();
                StringBuilder pluginsString = new StringBuilder();
                
                for (String plugin : plugins) {
                    String formattedPlugin = knownList.contains(plugin.toLowerCase()) 
                            ? Formatting.GREEN + plugin 
                            : Formatting.GRAY + plugin;
                    
                    if (!pluginsString.isEmpty()) {
                        pluginsString.append(Formatting.WHITE + ", ");
                    }
                    pluginsString.append(formattedPlugin);
                }
                
                logDirect(Text.literal(Formatting.WHITE + "Plugins (" + Formatting.RED + plugins.size() + Formatting.WHITE + "): " + pluginsString));
            } else {
                logDirect("Не удалось получить список плагинов!", Formatting.RED);
            }
            
            setState(false);
        }
    }
}
