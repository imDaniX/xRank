package me.imdanix.rank;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Command list
		if(args.length==0||args[0].equalsIgnoreCase("help")) {
			RankPlugin.helpMessages.forEach(sender::sendMessage);
			return true;
		}
		// Ranks info
		if(args[0].equalsIgnoreCase("info")) {
			if(args.length==1)
				RankPlugin.infoMessages.forEach(sender::sendMessage);
			else {
				Rank rank=RankPlugin.ranks.get(args[1]);
				if(rank!=null)
					rank.getDescription().forEach(sender::sendMessage);
				else
					sender.sendMessage(RankPlugin.noRank.replace("%rank", args[1]));
			}
			return true;
		}
		// Reload plugin
		if(args[0].equalsIgnoreCase("reload")) {
			if(!sender.hasPermission("xrank.reload"))
				return false;
			RankPlugin.getInstance().reloadConfig();
			RankPlugin.stopScheduler();
			RankPlugin.loadData();
			RankPlugin.loadRanks();
			RankPlugin.startScheduler();
			return true;
		}
		// Ranking
		Rank rank=RankPlugin.ranks.get(args[0]);
		if(rank!=null) {
			if(rank.rankUp((Player)sender))
				rank.execute((Player)sender);
			else
				sender.sendMessage(RankPlugin.noTime.replace("%rank", rank.getName()).replace("%time", ""+rank.getTime((Player)sender)/60000));
		} else
			sender.sendMessage(RankPlugin.noRank.replace("%rank", args[0]));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> variants=new ArrayList<>(RankPlugin.ranks.keySet());
		if(args.length==1)
			variants.add("info");
		List<String> completions = new ArrayList<>(variants);
		StringUtil.copyPartialMatches(args[0], variants, completions);
		Collections.sort(completions);
		return completions;
	}
}
