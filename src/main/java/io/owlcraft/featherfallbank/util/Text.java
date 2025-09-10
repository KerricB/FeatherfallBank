package io.owlcraft.featherfallbank.util;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Text {
    public static String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public static List<String> wrap(String line) {
        // simple single-line helper with color
        List<String> out = new ArrayList<>();
        out.add(color(line));
        return out;
    }
}
