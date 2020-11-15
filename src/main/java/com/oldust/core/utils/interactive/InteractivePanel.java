package com.oldust.core.utils.interactive;

import com.oldust.core.utils.CUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Un panel interactivo es aquel que
 * ejecuta un código a partir de una interacción
 * con determinado Item.
 */

public class InteractivePanel implements Listener {
    private final UUID player;
    private final ItemStack[] previousInventory;
    private final List<InteractiveItem> items = new ArrayList<>();

    public InteractivePanel(Player player) {
        this.player = player.getUniqueId();
        this.previousInventory = player.getInventory().getContents();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        if (!player.equals(e.getEntity().getUniqueId())) return;

        e.getDrops().clear();
        e.getDrops().addAll(Arrays.asList(previousInventory));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!player.equals(e.getPlayer().getUniqueId())) return;

        exit(e.getPlayer());
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!player.equals(e.getPlayer().getUniqueId())) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (!player.equals(e.getPlayer().getUniqueId()) || e.getItem() == null) return;

        items.stream()
                .filter(interactiveItem -> interactiveItem.getItem().isSimilar(e.getItem()))
                .findAny()
                .ifPresent(interactiveItem -> {
                    interactiveItem.getConsumer().accept(e);
                    e.setCancelled(true);
                });
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (!player.equals(e.getPlayer().getUniqueId())) return;

        boolean isInteractiveItem = items.stream()
                .anyMatch(interativeItem -> interativeItem.getItem().isSimilar(e.getItemDrop().getItemStack()));

        if (isInteractiveItem) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!player.equals(e.getWhoClicked().getUniqueId())) return;

        boolean isInteractiveItem = items.stream()
                .anyMatch(interativeItem -> interativeItem.getItem().isSimilar(e.getCurrentItem()));

        if (isInteractiveItem) {
            e.setCancelled(true);
        }

    }

    /**
     * Agrega un nuevo item al cual
     * el jugador puede interactuar.
     *
     * @param slot     slot en el cual el item se colocará
     * @param item     item que el jugador deberá presionar
     * @param consumer consumer que se ejecutará cada vez
     *                 que el jugador interactúe.
     */

    public void add(int slot, ItemStack item, Consumer<PlayerInteractEvent> consumer) {
        items.add(new InteractiveItem(slot, item, consumer));
    }

    /**
     * Colocar al jugador el panel.
     */

    public void enter(Player player) {
        CUtils.registerEvents(this);

        player.getInventory().clear();

        for (InteractiveItem interactiveItem : items) {
            if (interactiveItem == null) continue;

            player.getInventory().setItem(interactiveItem.getSlot(), interactiveItem.getItem());
        }
    }

    /**
     * Salir del modo y devolver
     * inventario anterior al jugador.
     */

    public void exit(Player player) {
        for (int i = 0; i < previousInventory.length; i++) {
            player.getInventory().setItem(i, previousInventory[i]);
        }

        CUtils.unregisterEvents(this);
    }

}
