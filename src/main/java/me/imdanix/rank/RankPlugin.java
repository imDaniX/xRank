package me.imdanix.rank;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class RankPlugin extends JavaPlugin {
    private static final long MINUTES_TO_TICKS = 60 * 20;

    private Map<String, Rank> ranks;
    public long timer;
    private BukkitTask scheduler;

    @Override
    public void onEnable() {
        ranks = new HashMap<>();
        saveDefaultConfig();
        Objects.requireNonNull(getCommand("xrank")).setExecutor(new CommandManager(this));
        loadData();
        loadRanks();
        startScheduler();
    }

    @Override
    public void onDisable() {
        stopScheduler();
    }

    public void loadData() {
        FileConfiguration cfg = getConfig();
        timer = cfg.getLong("settings.timer") * MINUTES_TO_TICKS;
        Msg.reload(cfg.getConfigurationSection("messages"));
    }

    public void loadRanks() {
        ranks.clear();
        ConfigurationSection ranksSec = getConfig().getConfigurationSection("ranks");
        for (String name : ranksSec.getKeys(false)) {
            ranks.put(name.toLowerCase(), new Rank(name, ranksSec.getConfigurationSection(name)));
        }
    }

    public Map<String, Rank> getRanks() {
        return ranks;
    }

    public void startScheduler() {
        scheduler = Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers())
                for (Rank r : ranks.values()) {
                    if (r.rankUp(p) == Rank.CheckResult.SUCCESS)
                        break;
                }
        }, timer, timer);
    }

    public void stopScheduler() {
        scheduler.cancel();
    }
}
