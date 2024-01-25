package me.imdanix.rank;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;

import static me.imdanix.rank.Msg.clr;

public class Rank {
    private static final long TICK_TO_MS = 1000 / 20;

    private final long time;
    private final String name;
    private final String permission;
    private final String gotPermission;
    private final List<String> description;
    private final List<String> commands;
    private final boolean fromJoin;
    private final boolean auto;
    private final boolean broadcast;

    public Rank(String id, ConfigurationSection cfg) {
        this(
                id,
                cfg.getLong("minutes") * 60000L,
                cfg.getString("name"),
                cfg.getStringList("description"),
                cfg.getStringList("commands"),
                cfg.getBoolean("from-join", false),
                cfg.getBoolean("auto", true),
                cfg.getBoolean("broadcast", true)
        );
    }

    /**
     * @param id Id of this rank
     * @param time How many millis player should have to rank up
     * @param name Name of this rank. Colors can be used
     * @param description Description of this rank. Colors can be used
     * @param commands List of commands that will be executed on rank up
     * @param fromJoin Is time from first join or player's statistic
     * @param auto Will player gain this rank automatically
     * @param broadcast Do you want to broadcast about rank up
     */
    public Rank(String id, long time, String name, List<String> description, List<String> commands, boolean fromJoin, boolean auto, boolean broadcast) {
        this.time = time;

        this.name = clr(name);
        this.description = clr(description);
        this.commands = commands;
        this.permission = "xrank.rank." + id;
        this.gotPermission = "xrank.rank." + id + ".got";

        this.fromJoin = fromJoin;
        this.auto = auto;
        this.broadcast = broadcast;
    }

    /**
     * Trying to rank up if possible or showing that it's possible
     * @param p Player to rank up
     * @return Does player have access
     */
    public CheckResult rankUp(Player p) {
        CheckResult result = hasAccess(p);
        if (result != CheckResult.SUCCESS) {
            return result;
        }
        if (auto) {
            execute(p);
        }
        return result;
    }

    public void execute(Player p) {
        if (broadcast) {
            Bukkit.broadcastMessage(
                    Msg.BROADCAST.get()
                            .replace("%player", p.getName())
                            .replace("%rank", name)
            );
        }
        p.sendMessage(
                Msg.GETTING.get()
                        .replace("%player", p.getName())
                        .replace("%rank", name)
        );
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        commands.forEach(cmd -> Bukkit.dispatchCommand(console, cmd.replace("%player", p.getName())));
    }

    /**
     * Check access to gain this rank
     * @param p Player to check
     * @return Can player gain that rank
     */
    public CheckResult hasAccess(Player p) {
        if (p.hasPermission(gotPermission)) return CheckResult.ALREADY_GOT;
        if (!p.hasPermission(permission)) return CheckResult.NO_ACCESS;
        if (getTime(p) > 0) return CheckResult.NOT_ENOUGH;
        return CheckResult.SUCCESS;
    }

    /**
     * Get player's time to gain this rank
     * @param p Player to check
     * @return Time to gain this rank in milliseconds
     */
    public long getTime(Player p) {
        return time - _getTime(p);
    }

    private long _getTime(Player p) {
        return fromJoin
                ? System.currentTimeMillis() - p.getFirstPlayed()
                : ((long) p.getStatistic(Statistic.PLAY_ONE_MINUTE)) * TICK_TO_MS;
    }

    public List<String> getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String debug() {
        return time + "ms" + (fromJoin ? " join" : " play") + (auto ? ", auto" : ", manual");
    }

    public enum CheckResult {
        SUCCESS, NO_ACCESS, NOT_ENOUGH, ALREADY_GOT
    }
}
