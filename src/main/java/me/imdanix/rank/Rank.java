package me.imdanix.rank;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

import static me.imdanix.rank.RankPlugin.clr;

public class Rank {
	private int minutes;
	private String name;
	private String description;
	private String permission;
	private List<String> commands;
	private boolean auto;
	private boolean broadcast;

	public Rank(String id, ConfigurationSection section) {
		this(section.getInt("minutes"),
				section.getString("name"),
				section.getString("description"),
				"xrank.rank."+id,
				section.getStringList("commands"),
				section.getBoolean("auto"),
				section.getBoolean("broadcast"));
	}

	public Rank(int minutes, String name, String description, String permission, List<String> commands, boolean auto, boolean broadcast) {
		this.minutes=minutes;
		this.name=clr(name);
		this.description=clr(description);
		this.commands=commands;
		this.permission=permission;
		this.auto=auto;
		this.broadcast=broadcast;
	}

	public boolean rankUp(Player p) {
		if(!haveAccess(p))
			return false;
		p.sendMessage(description);
		if(auto)
			execute(p);
		else
			p.sendMessage(RankPlugin.gettingMessage.replace("%rank", name).replace("%player", p.getName()));
		return true;
	}

	public boolean execute(Player p) {
		if(broadcast)
			Bukkit.broadcastMessage(RankPlugin.broadcastMessage.replace("%player", p.getName()).replace("%rank", name));
		ConsoleCommandSender console=Bukkit.getConsoleSender();
		Bukkit.getScheduler().runTask(RankPlugin.getInstance(),
				() -> commands.forEach(cmd->Bukkit.dispatchCommand(console, cmd.replace("%player", p.getName()))));
		return true;
	}

	public boolean haveAccess(Player p) {
		if(!checkTime(p))
			return false;
		return RankPlugin.getMode()==p.hasPermission(permission);
	}

	public boolean checkTime(Player p) {
		return minutes<=p.getStatistic(Statistic.PLAY_ONE_MINUTE);
	}

	public int getTime(Player p) {
		return minutes-p.getStatistic(Statistic.PLAY_ONE_MINUTE);
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}
}
