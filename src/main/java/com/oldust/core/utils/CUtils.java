package com.oldust.core.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CUtils {

    private CUtils() {}

    public static void logConsole(String message) {
        Bukkit.getConsoleSender().sendMessage(color(message));
    }

    public static void inform(String prefix, String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE
                + "[" + prefix + "]"
                + ChatColor.DARK_GRAY
                + " -> " + ChatColor.RESET
                + message);
    }

    public static void msg(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    public static String color(String text) {
        return text.replace("&", "§");
    }

}
