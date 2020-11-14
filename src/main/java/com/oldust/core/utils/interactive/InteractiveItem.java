package com.oldust.core.utils.interactive;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

@Getter
@RequiredArgsConstructor
public class InteractiveItem {
    private final int slot;
    private final ItemStack item;
    private final Consumer<PlayerInteractEvent> consumer;
}
