package me.imdanix.rank;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

import static me.imdanix.rank.RankPlugin.clr;

public class Rank {
	private final long time;
	private final String name;
	private final String permission;
	private final List<String> description;
	private final List<String> commands;
	private final boolean fromJoin;
	private final boolean auto;
	private final boolean broadcast;

	public Rank(String id, ConfigurationSection section) {
		this(section.getLong("minutes")*60000,
				section.getString("name"),
				section.getStringList("description"),
				"xrank.rank."+id,
				section.getStringList("commands"),
				section.getBoolean("from_join"),
				section.getBoolean("auto"),
				section.getBoolean("broadcast"));
	}

	/**
	 * @param time How many milliseconds player should have to rankup
	 * @param name Name of this rank. Colors can be used
	 * @param description Description of this rank. Colors can be used
	 * @param permission Permission of this rank
	 * @param commands List of commands that will be executed on rankup
	 * @param fromJoin Is time from first join or player's statistic
	 * @param auto Will player gain this rank automatically
	 * @param broadcast Do you want to broadcast about rankup
	 */
	public Rank(long time, String name, List<String> description, String permission, List<String> commands, boolean fromJoin, boolean auto, boolean broadcast) {
		this.time = time;

		this.name = clr(name);
		this.description = clr(description);
		this.commands = commands;
		this.permission = permission;

		this.fromJoin = fromJoin;
		this.auto = auto;
		this.broadcast = broadcast;
	}

	/**
	 * Trying to rankup if possible or showing that it's possible
	 * @param p Player to rankup
	 * @return Does player have access
	 */
	public boolean rankUp(Player p) {
		if(!hasAccess(p))
			return false;
		description.forEach(s->p.sendMessage(s.replace("%player", p.getName())));
		if(auto)
			execute(p);
		return true;
	}

	public void execute(Player p) {
		if(broadcast) Bukkit.broadcastMessage(
				RankPlugin.broadcastMessage
				.replace("%player", p.getName())
				.replace("%rank", name));
		p.sendMessage(
				RankPlugin.gettingMessage
				.replace("%player", p.getName())
				.replace("%rank", name));
		ConsoleCommandSender console = Bukkit.getConsoleSender();
		commands.forEach(cmd -> Bukkit.dispatchCommand(console, cmd.replace("%player", p.getName())));
	}

	/**
	 * Check access to gain this rank
	 * @param p Player to check
	 * @return Can player gain that rank
	 */
	public boolean hasAccess(Player p) {
		return checkTime(p) && RankPlugin.getMode() == p.hasPermission(permission);
	}

	/**
	 * Check player's time compared to rank's request
	 * @param p Player to check
	 * @return Does player enough time to gain that rank
	 */
	public boolean checkTime(Player p) {
		return time <= (fromJoin ? (System.currentTimeMillis()-p.getFirstPlayed()) : p.getStatistic(Statistic.PLAY_ONE_MINUTE)*50);
	}

	/**
	 * Get player's time to gain this rank
	 * @param p Player to check
	 * @return Time to gain this rank in milliseconds
	 */
	public long getTime(Player p) {
		return time - (fromJoin ? (System.currentTimeMillis()-p.getFirstPlayed()) : p.getStatistic(Statistic.PLAY_ONE_MINUTE)*50);
	}

	public List<String> getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}
}
