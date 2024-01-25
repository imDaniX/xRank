package me.imdanix.rank;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CommandManager implements CommandExecutor, TabCompleter {
    private static final NumberFormat FORMAT = new DecimalFormat("#.0", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final double MS_TO_HOURS = 1000 * 60 * 60;

    private final RankPlugin plugin;

    public CommandManager(RankPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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
                if (rank != null) {
                    sender.sendMessage(rank.debug());
                }
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
            Rank.CheckResult result = rank.rankUp((Player) sender);
            switch (result) {
                case SUCCESS -> {
                    rank.getDescription().forEach(s -> sender.sendMessage(s.replace("%player", sender.getName())));
                }
                case ALREADY_GOT -> {
                    sender.sendMessage(Msg.ALREADY_GOT.get().replace("%rank", rank.getName()));
                }
                case NOT_ENOUGH -> {
                    sender.sendMessage(Msg.NO_TIME.get()
                            .replace("%rank", rank.getName())
                            .replace("%time", FORMAT.format(rank.getTime((Player) sender) / MS_TO_HOURS))
                    );
                }
                case NO_ACCESS -> {
                    sender.sendMessage(Msg.NO_ACCESS.get().replace("%rank", rank.getName()));
                }
            }
        } else {
            sender.sendMessage(Msg.NO_RANK.get().replace("%rank", args[0]));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
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
}
