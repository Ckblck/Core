package net.oldust.core.utils;

import lombok.experimental.UtilityClass;
import net.md_5.bungee.api.ChatColor;
import net.oldust.core.Core;
import net.oldust.core.VelocityCore;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

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
    public boolean IS_PROXY;

    private final Pattern COLOR_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}( )?");

    public void warnSyncCall() {
        try {
            if (!IS_PROXY && Bukkit.isPrimaryThread() && Bukkit.getPluginManager().isPluginEnabled("Conquer")) { // Conquer uses this method explicitly.
                Thread.dumpStack();

                inform("Server", Lang.ERROR_COLOR + "WARNING! A call from the Main thread was made, when expected Async usage.");
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

        if (lastChar == 'w' || lastChar == 'd') {
            return Period.parse("P" + duration.toUpperCase());
        } else if (Character.isUpperCase(lastChar)) {
            return Period.parse("P" + duration);
        } else {
            return Duration.parse("PT" + duration);
        }

    }

    public void inform(String prefix, String message) {
        String finalMessage = ChatColor.BLUE
                + "[" + prefix + "]"
                + ChatColor.DARK_GRAY
                + " -> " + ChatColor.RESET
                + message;

        if (!IS_PROXY) {
            Bukkit.getServer().getConsoleSender().sendMessage(finalMessage);
        } else {
            VelocityCore.getInstance().getLogger().info(finalMessage);
        }
    }

    /**
     * Sends a message to a CommandSender.
     */

    public void msg(CommandSender sender, String message) {
        sender.sendMessage(color(message));
    }

    /**
     * Sends a message with a sound.
     */

    public void msg(CommandSender sender, String message, @Nullable LangSound sound) {
        sender.sendMessage(color(message));

        if (sound != null && sender instanceof Player) {
            Player player = (Player) sender;

            sound.play(player);
        }

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
        if (IS_PROXY || Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(Core.getInstance(), runnable);
        }
    }

    public void runAsync(Runnable runnable) {
        if (IS_PROXY || !Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(Core.getInstance(), runnable);
        }
    }

}
