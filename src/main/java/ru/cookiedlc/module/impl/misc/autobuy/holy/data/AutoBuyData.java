package ru.cookiedlc.module.impl.misc.autobuy.holy.data;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import ru.cookiedlc.module.impl.misc.autobuy.holy.item.EnumItemType;

import java.util.*;

@Getter
public class AutoBuyData {
    private static final AutoBuyData INSTANCE = new AutoBuyData();
    
    private final HashMap<EnumItemType, Integer> priceForOne = new HashMap<>();
    private final HashSet<EnumItemType> disabledItems = new HashSet<>();
    private final HashSet<String> staffNicknames = new HashSet<>();
    private final List<BuyHistory> history = new ArrayList<>();
    
    @Setter
    private double multiplier = 0.65;
    @Setter
    private int refreshDelay = 30;
    @Setter
    private String status = "N/A";
    @Setter
    private int itemsBought = 0;
    @Setter
    private boolean antiDetectEnabled = true;
    
    @Setter
    private String botToken = "";
    @Setter
    private String chatId = "";
    @Setter
    private boolean botEnabled = false;
    
    public static AutoBuyData getInstance() {
        return INSTANCE;
    }
    
    public int getMoney() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return -1;
        
        Scoreboard scoreboard = mc.world.getScoreboard();
        if (scoreboard == null) return -1;
        
        return Optional.ofNullable(scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR))
                .map(scoreboard::getScoreboardEntries)
                .stream()
                .flatMap(Collection::stream)
                .map(score -> {
                    String line = score.owner();
                    Team team = scoreboard.getScoreHolderTeam(score.owner());
                    if (team != null) {
                        line = Team.decorateName(team, Text.of(line)).getString();
                    }
                    return line;
                })
                .filter(line -> line.contains("Монеток: "))
                .findFirst()
                .map(line -> {
                    String digitsOnly = line.replaceAll("[^\\d]", "");
                    if (digitsOnly.length() > 1) {
                        digitsOnly = digitsOnly.substring(0, digitsOnly.length() - 1);
                    }
                    return digitsOnly.isEmpty() ? -1 : Integer.parseInt(digitsOnly);
                })
                .orElse(-1);
    }
    
    public void addStaff(String nickname) {
        staffNicknames.add(nickname.toLowerCase());
    }
    
    public void removeStaff(String nickname) {
        staffNicknames.remove(nickname.toLowerCase());
    }
    
    public void clearPrices() {
        priceForOne.clear();
    }
    
    public void clearHistory() {
        history.clear();
    }
    
    public record BuyHistory(EnumItemType itemType, int price, int allPrice, int auctionPrice, int balance) {}
}
