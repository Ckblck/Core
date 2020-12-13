package net.oldust.core.interactive.panels;

import org.bukkit.entity.Player;

import java.io.Serializable;

public interface InteractiveListener extends Serializable {
    default void onInventoryClick(Player player, InteractiveItem item) {
    }

    default void onInteract(Player player, InteractiveItem item) {
    }

    default void onExit(Player player) {
    }
}
