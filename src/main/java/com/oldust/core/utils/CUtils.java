package com.oldust.core.utils;

import com.oldust.core.Core;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CUtils {

    private CUtils() {
    }

    public static void warnSyncCall() {
        if (Bukkit.isPrimaryThread()) {
            inform("SERVER", Lang.ERROR_COLOR + "¡IMPORTANTE! Se realizó una llamada a un método que debía ser Async el el main thread.");
        }
    }

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

    public static void registerEvents(Listener listener) {
        Core.getInstance().getServer().getPluginManager().registerEvents(listener, Core.getInstance());
    }

    public static void unregisterEvents(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    public static List<String> colorizeList(List<String> list) {
        return list.stream()
                .map(line -> line = color(line))
                .collect(Collectors.toList());
    }

    public static String[] colorizeArray(String[] array) {
        return Arrays.stream(array)
                .map(CUtils::color)
                .toArray(String[]::new);
    }

}
