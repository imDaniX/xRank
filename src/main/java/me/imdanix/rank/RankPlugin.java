package me.imdanix.rank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RankPlugin extends JavaPlugin {

    private static JavaPlugin instance;
    public static Map<String, Rank> ranks;
    public static long timer;
    private static int scheduler;

    public static String gettingMessage, broadcastMessage, noRank, noTime;
    public static List<String> helpMessages, infoMessages;

    @Override
    public void onEnable() {
        instance = this;
        ranks = new HashMap<>();
        saveDefaultConfig();
        getCommand("xrank").setExecutor(new CommandManager());
        loadData();
        loadRanks();
        startScheduler();
    }

    @Override
    public void onDisable() {
        stopScheduler();
    }

    public static void loadData() {
        FileConfiguration cfg = instance.getConfig();
        timer = cfg.getLong("settings.timer")*1200;
        gettingMessage = clr(cfg.getString("settings.getting_message").replace("\\n", "\n"));
        broadcastMessage = clr(cfg.getString("settings.broadcast_message").replace("\\n", "\n"));
        noRank = clr(cfg.getString("settings.no_rank").replace("\\n", "\n"));
        noTime = clr(cfg.getString("settings.no_time").replace("\\n", "\n"));
        helpMessages = clr(cfg.getStringList("settings.help_messages"));
        infoMessages = clr(cfg.getStringList("settings.info_messages"));
    }

    public static void loadRanks() {
        ranks.clear();
        ConfigurationSection ranksSec = instance.getConfig().getConfigurationSection("ranks");
        for(String name : ranksSec.getKeys(false))
            ranks.put(name.toLowerCase(), new Rank(name, ranksSec.getConfigurationSection(name)));
    }

    public static void startScheduler() {
        scheduler = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, () -> {
            for(Player p : Bukkit.getOnlinePlayers())
                for(Rank r : ranks.values()) {
                    if(r.rankUp(p))
                        break;
                }
        }, timer, timer);
    }

    public static void stopScheduler() {
        Bukkit.getScheduler().cancelTask(scheduler);
    }

    public static JavaPlugin getInstance() {
        return instance;
    }

    static String clr(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    static List<String> clr(List<String> ls) {
        List<String> clred = new ArrayList<>();
        ls.forEach(s->clred.add(clr(s)));
        return clred;
    }
}
