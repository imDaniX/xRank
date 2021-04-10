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
    private final String gotPermission;
    private final List<String> description;
    private final List<String> commands;
    private final boolean fromJoin;
    private final boolean auto;
    private final boolean broadcast;

    public Rank(String id, ConfigurationSection cfg) {
        this(cfg.getLong("minutes") * 60000,
                cfg.getString("name"),
                cfg.getStringList("description"),
                "xrank.rank."+id,
                cfg.getStringList("commands"),
                cfg.getBoolean("from_join", false),
                cfg.getBoolean("auto", true),
                cfg.getBoolean("broadcast", true));
    }

    /**
     * @param time How many millis player should have to rankup
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
        this.gotPermission = permission + ".got";

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
        description.forEach(s -> p.sendMessage(s.replace("%player", p.getName())));
        if(auto)
            execute(p);
        return true;
    }

    public void execute(Player p) {
        if(broadcast) Bukkit.broadcastMessage(
                RankPlugin.broadcastMessage
                .replace("%player", p.getName())
                .replace("%rank", name)
        );
        p.sendMessage(
                RankPlugin.gettingMessage
                .replace("%player", p.getName())
                .replace("%rank", name))
        ;
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        commands.forEach(cmd -> Bukkit.dispatchCommand(console, cmd.replace("%player", p.getName())));
    }

    /**
     * Check access to gain this rank
     * @param p Player to check
     * @return Can player gain that rank
     */
    public boolean hasAccess(Player p) {
        return p.hasPermission(permission) && !p.hasPermission(gotPermission) && getTime(p) <= 0;
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
        return fromJoin ?
                System.currentTimeMillis() - p.getFirstPlayed() :
                ((long)p.getStatistic(Statistic.PLAY_ONE_MINUTE)) * 50L;
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
}
