package me.imdanix.rank;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

public class Rank {
	private int minutes;
	private String name;
	private String description;
	private String permission;
	private List<String> commands;
	private boolean auto;
	private boolean broadcast;

	public Rank(String name, ConfigurationSection section) {
		this(section.getInt("minutes"),
				RankPlugin.clr(section.getString("name")),
				section.getString("description"),
				"xrank.rank."+name,
				section.getStringList("commands"),
				section.getBoolean("auto"),
				section.getBoolean("broadcast"));
	}

	public Rank(int minutes, String name, String description, String permission, List<String> commands, boolean auto, boolean broadcast) {
		this.minutes=minutes;
		this.name=name;
		this.description=description;
		this.commands=commands;
		this.permission=permission;
		this.auto=auto;
		this.broadcast=broadcast;
	}

	public boolean rankUp(Player p) {
		if(!(checkTime(p)&&p.hasPermission(permission)))
			return false;
		p.sendMessage(description);
		if(auto)
			execute(p);
		else
			p.sendMessage(RankPlugin.gettingMessage.replace("%rank", name));
		return true;
	}

	public boolean execute(Player p) {
		if(broadcast)
			Bukkit.broadcastMessage(RankPlugin.broadcastMessage.replace("%player", p.getName()).replace("%rank", name));
		ConsoleCommandSender console=Bukkit.getConsoleSender();
		commands.forEach(cmd->Bukkit.dispatchCommand(console, cmd.replace("%player", p.getName())));
		return true;
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
