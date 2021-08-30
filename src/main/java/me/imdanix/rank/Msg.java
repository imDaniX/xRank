package me.imdanix.rank;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum Msg {
    GETTING, BROADCAST, NO_TIME, NO_RANK, HELP, INFO;

    private final String path;
    private List<String> text;

    Msg() {
        this.path = name().toLowerCase().replace('_', '-');
        this.text = Collections.singletonList(name());
    }

    public String get() {
        return text.get(0);
    }

    public List<String> getList() {
        return text;
    }

    public static void reload(ConfigurationSection cfg) {
        for (Msg msg : values()) {
            if (cfg.isList(msg.path)) {
                msg.text = cfg.getStringList(msg.path);
                if (msg.text.isEmpty()) {
                    msg.text = Collections.singletonList(msg.name());
                } else {
                    msg.text.replaceAll(Msg::clr);
                }
            } else {
                msg.text = Collections.singletonList(clr(cfg.getString(msg.path, msg.name())));
            }
        }
    }

    public static String clr(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public static List<String> clr(List<String> ls) {
        List<String> clred = new ArrayList<>();
        ls.forEach(s -> clred.add(clr(s)));
        return clred;
    }
}
