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
    private final RankPlugin plugin;

    public CommandManager(RankPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Command list
        if (args.length == 0 || (args[0] = args[0].toLowerCase()).equals("help")) {
            Msg.HELP.getList().forEach(sender::sendMessage);
            return true;
        }
        // Ranks info
        if (args[0].equals("info")) {
            if (args.length == 1) {
                Msg.INFO.getList().forEach(sender::sendMessage);
            } else {
                Rank rank = plugin.getRanks().get(args[1]);
                if (rank != null) {
                    rank.getDescription().forEach(s -> sender.sendMessage(s.replace("%player", sender.getName())));
                } else {
                    sender.sendMessage(Msg.NO_RANK.get().replace("%rank", args[1]));
                }
            }
            return true;
        }
        // Debug
        if (args[0].equals("debug")) {
            if (!sender.hasPermission("xrank.debug")) {
                return false;
            } if (args.length == 1) {
                Msg.INFO.getList().forEach(sender::sendMessage);
            } else {
                Rank rank = plugin.getRanks().get(args[1]);
                if (rank != null)
                    sender.sendMessage(rank.debug());
            }
            return true;
        }
        // Reload plugin
        if (args[0].equals("reload")) {
            if (!sender.hasPermission("xrank.reload")) {
                return false;
            }
            plugin.reloadConfig();
            plugin.stopScheduler();
            plugin.loadData();
            plugin.loadRanks();
            plugin.startScheduler();
            return true;
        }
        // Ranking
        Rank rank = plugin.getRanks().get(args[0]);
        if (rank != null) {
            if (!rank.rankUp((Player) sender)) {
                sender.sendMessage(
                        Msg.NO_TIME.get()
                                .replace("%rank", rank.getName())
                                .replace("%time", Double.toString(fix(rank.getTime((Player) sender) / 3600000D))));
            }
        } else {
            sender.sendMessage(Msg.NO_RANK.get().replace("%rank", args[0]));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> variants = new ArrayList<>(plugin.getRanks().keySet());
        String arg = args[0];
        if (args.length == 1) {
            variants.add("info");
        } else if (args.length == 2) {
            arg = args[1];
        }
        List<String> completions = new ArrayList<>(variants);
        StringUtil.copyPartialMatches(arg, variants, completions);
        Collections.sort(completions);
        return completions;
    }

    private static double fix(double a) {
        double j = a * 10;
        boolean cut = false;
        while (true) {
            j = (j * 10) % 1;
            double b = j * 10;
            if (b < 6) {
                if (b < 5) {
                    cut = true;
                    break;
                }
            } else break;
        }
        return (Math.floor(a)) + (Math.floor(a % 1 * 100) + (cut ? 0 : 1)) / 100;
    }
}
