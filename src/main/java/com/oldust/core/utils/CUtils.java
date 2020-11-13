package com.oldust.core.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

public class CUtils {

    private CUtils() {}

    public static void log(String prefix, String message) {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.of("#47ffe0")
                + "(" + prefix
                + ChatColor.of("#47ffe0")
                + ")" + ChatColor.of("#868787")
                + message);
    }

}
