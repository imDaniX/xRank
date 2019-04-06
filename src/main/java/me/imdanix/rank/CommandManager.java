package me.imdanix.rank;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length==0) {
			RankPlugin.helpMessages.forEach(sender::sendMessage);
			return true;
		}
		if(args[0].equalsIgnoreCase("info")) {
			if(args.length==1)
				RankPlugin.infoMessages.forEach(sender::sendMessage);
			else {
				Rank rank=RankPlugin.ranks.get(args[1]);
				if(rank!=null)
					sender.sendMessage(rank.getDescription());
				else
					sender.sendMessage(RankPlugin.noRank.replace("%rank", args[1]));
			}
			return true;
		}
		if(args[0].equalsIgnoreCase("reload")) {
			if(!sender.hasPermission("xrank.reload"))
				return false;
			RankPlugin.getInstance().reloadConfig();
			RankPlugin.stopScheduler();
			RankPlugin.loadData();
			RankPlugin.loadRanks();
			RankPlugin.startScheduler();
		}
		Rank rank=RankPlugin.ranks.get(args[1]);
		if(rank!=null) {
			if(rank.rankUp((Player)sender))
				rank.execute((Player)sender);
			else
				sender.sendMessage(RankPlugin.noTime.replace("%rank", rank.getName()).replace("%time", ""+rank.getTime((Player)sender)));
		} else
			sender.sendMessage(RankPlugin.noRank.replace("%rank", args[1]));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		return null;
	}
}
