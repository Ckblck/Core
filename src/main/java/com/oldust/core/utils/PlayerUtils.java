package com.oldust.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public class PlayerUtils {

    private PlayerUtils() {
    }

    public static Collection<? extends Player> getPlayers() {
        return Bukkit.getOnlinePlayers();
    }

}
