package me.imdanix.rank;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public final class RankPlugin extends JavaPlugin {

	private static RankPlugin instance;
	public static Set<Rank> ranksSet;
	public static int timer;
	private static BukkitTask scheduler;

	@Override
	public void onEnable() {
		instance=this;
		CommandManager cm=new CommandManager();
		getCommand("xrank").setExecutor(cm);
		getCommand("xrankreload").setExecutor(cm);
		getCommand("xrank").setTabCompleter(cm);
		timer=getConfig().getInt("timer")*20;
		loadRanks();
	}

	public void loadRanks() {
		ranksSet=new HashSet<>();
		ConfigurationSection ranks=this.getConfig().getConfigurationSection("ranks");
		for(String name:ranks.getKeys(false)) {
			ConfigurationSection rankSection=ranks.getConfigurationSection(name);
			Rank rank=new Rank(rankSection.getInt("minutes"),
							   rankSection.getString("name"),
							   rankSection.getString("description"),
							   rankSection.getString("getting"),
							   "xrank.rank."+name,
							   rankSection.getStringList("commands"),
							   rankSection.getBoolean("auto"));
			ranksSet.add(rank);
		}
	}

	public static void startScheduler() {
		scheduler = new BukkitRunnable() {
			@Override
			public void run() {
				for(Player p : Bukkit.getOnlinePlayers())
					for(Rank r : ranksSet)
						if(r.execute(p))
							break;
			}
		}.runTaskTimerAsynchronously(instance, timer, timer);
	}

	public static void stopScheduler() {
		scheduler.cancel();
	}
}
