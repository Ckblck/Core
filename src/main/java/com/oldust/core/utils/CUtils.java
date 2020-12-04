package com.oldust.core.utils;

import com.oldust.core.Core;
import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class CUtils {
    private final Pattern COLOR_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}( )?");

    public void warnSyncCall() {
        try {
            if (Bukkit.isPrimaryThread()) {
                Thread.dumpStack();

                inform("SERVER", Lang.ERROR_COLOR + "WARNING! A call from the Main thread was made, when expected Async usage.");
            }
        } catch (NullPointerException ignored) {
        } // BungeeCord usage.
    }

    /**
     * Obtiene una duración a partir
     * de un string. Ejemplo: 1w, 1m (minute), 1M (month)
     */

    public TemporalAmount parseLiteralTime(String duration) {
        char lastChar = duration.charAt(duration.length() - 1);

        if (lastChar == 'w') {
            return Period.parse("P" + duration.toUpperCase());
        } else if (Character.isUpperCase(lastChar)) {
            return Period.parse("P" + duration);
        } else {
            return Duration.parse("PT" + duration);
        }

    }

    public void logConsole(String message) {
        Core.getInstance().getServer().getConsoleSender().sendMessage(color(message));
    }

    public void inform(String prefix, String message) {
        Core.getInstance().getServer().getConsoleSender().sendMessage(ChatColor.BLUE
                + "[" + prefix + "]"
                + ChatColor.DARK_GRAY
                + " -> " + ChatColor.RESET
                + message);
    }

    public void msg(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    /**
     * Colorea una string, ya sea con #404899 o &6.
     * Se recomienda agregar un espacio para # de esta manera:
     * {@code #404899 Coloured text} para claridad en el código.
     * Este método quitará el último espacio EN CASO que lo tenga.
     */

    public String color(String text) {
        Matcher matcher = COLOR_PATTERN.matcher(text);

        while (matcher.find()) {
            String color = text.substring(matcher.start(), matcher.end());
            StringBuilder buffer = new StringBuilder(text);

            if (color.endsWith(" ")) {
                buffer.replace(matcher.end() - 1, matcher.end(), "");
                color = color.trim();
            }

            text = buffer.toString().replace(color, ChatColor.of(color) + "");
            matcher = COLOR_PATTERN.matcher(text);
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public void registerEvents(Listener listener) {
        Bukkit.getServer().getPluginManager().registerEvents(listener, Core.getInstance());
    }

    public void unregisterEvents(Listener listener) {
        HandlerList.unregisterAll(listener);
    }

    public List<String> colorizeList(List<String> list) {
        return list.stream()
                .map(line -> line = color(line))
                .collect(Collectors.toList());
    }

    public String[] colorizeArray(String[] array) {
        return Arrays.stream(array)
                .map(CUtils::color)
                .toArray(String[]::new);
    }

    public void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(Core.getInstance(), runnable);
    }

    public void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), runnable);
    }

}
