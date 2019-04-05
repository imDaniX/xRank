package me.imdanix.rank;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class Rank {
	private int minutes;
	private String name;
	private String description;
	private String getting;
	private String permission;
	private List<String> commands;
	private boolean auto;

	public Rank(int minutes, String name, String description, String getting, String permission, List<String> commands, boolean auto) {
		this.minutes=minutes;
		this.name=name;
		this.description=description;
		this.getting=getting;
		this.commands=commands;
		this.permission=permission;
		this.auto=auto;
	}

	public boolean run(Player p) {
		if(!(checkTime(p)&&p.hasPermission(permission)))
			return false;
		if(auto) {
			p.sendMessage(getting);
			execute(p);
		} else
			p.sendMessage(description);
		return true;
	}

	public boolean execute(Player p) {
		ConsoleCommandSender console=Bukkit.getConsoleSender();
		for(String cmd:commands)
			Bukkit.dispatchCommand(console, cmd.replace("%player", p.getName()));
		return true;
	}

	public boolean checkTime(Player p) {
		return minutes<=p.getStatistic(Statistic.PLAY_ONE_MINUTE);
	}
}
