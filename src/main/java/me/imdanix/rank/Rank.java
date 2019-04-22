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
	private String permission;
	private List<String> description;
	private List<String> commands;
	private boolean fromJoin;
	private boolean auto;
	private boolean broadcast;

	public Rank(String id, ConfigurationSection section) {
		this(section.getInt("minutes"),
				section.getString("name"),
				section.getStringList("description"),
				"xrank.rank."+id,
				section.getStringList("commands"),
				section.getBoolean("from_join"),
				section.getBoolean("auto"),
				section.getBoolean("broadcast"));
	}

	public Rank(int minutes, String name, List<String> description, String permission, List<String> commands, boolean fromJoin, boolean auto, boolean broadcast) {
		this.minutes=minutes;
		this.name=clr(name);
		this.description=clr(description);
		this.commands=commands;
		this.permission=permission;
		this.fromJoin=fromJoin;
		this.auto=auto;
		this.broadcast=broadcast;
	}

	public boolean rankUp(Player p) {
		if(!haveAccess(p))
			return false;
		description.forEach(p::sendMessage);
		if(auto)
			return execute(p);
		return true;
	}

	public boolean execute(Player p) {
		if(broadcast)
			Bukkit.broadcastMessage(RankPlugin.broadcastMessage.replace("%player", p.getName()).replace("%rank", name));
		p.sendMessage(RankPlugin.gettingMessage.replace("%rank", name).replace("%player", p.getName()));
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
		double time=p.getStatistic(Statistic.PLAY_ONE_MINUTE);
		if(fromJoin)
			time=p.getFirstPlayed()/60000;
		return minutes<=time;
	}

	public int getTime(Player p) {
		return minutes-p.getStatistic(Statistic.PLAY_ONE_MINUTE);
	}

	public List<String> getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}
}
