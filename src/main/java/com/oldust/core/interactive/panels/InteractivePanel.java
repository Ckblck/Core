package com.oldust.core.interactive.panels;

import com.oldust.core.Core;
import com.oldust.core.commons.EventsProvider;
import com.oldust.core.commons.Operation;
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

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Un panel interactivo es aquel que
 * ejecuta un código a partir de una interacción
 * con determinado Item.
 */

public class InteractivePanel implements Listener, Serializable {
    private final UUID player;
    private final transient ItemStack[] previousInventory;
    private final transient List<InteractiveItem> items = new ArrayList<>();
    private final Set<InteractiveListener> listeners = new HashSet<>();

    public InteractivePanel(Player player) {
        this.player = player.getUniqueId();
        this.previousInventory = player.getInventory().getContents();

        joinQuitEvents();
    }

    public void joinQuitEvents() {
        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(PlayerQuitEvent.class, new Operation<PlayerQuitEvent>((quit, db) -> {
            Player player = quit.getPlayer();
            UUID uuid = player.getUniqueId();

            if (!uuid.equals(this.player)) return;

            exit(player);
        }));

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        if (isNotSamePlayer(e.getEntity())) return;

        e.getDrops().clear();
        e.getDrops().addAll(Arrays.asList(previousInventory));
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (isNotSamePlayer(e.getPlayer())) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (isNotSamePlayer(player) || e.getItem() == null) return;

        e.setCancelled(true);

        items.stream()
                .filter(interactiveItem -> interactiveItem.getItem().isSimilar(e.getItem()))
                .findAny()
                .ifPresent(interactiveItem -> {
                    interactiveItem.getConsumer().accept(e);

                    listeners.forEach(listener -> listener.onInteract(player, interactiveItem));
                });
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (isNotSamePlayer(e.getPlayer())) return;

        boolean isInteractiveItem = items.stream()
                .anyMatch(interativeItem -> interativeItem.getItem().isSimilar(e.getItemDrop().getItemStack()));

        if (isInteractiveItem) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (isNotSamePlayer((Player) e.getWhoClicked())) return;

        Player player = (Player) e.getWhoClicked();
        AtomicReference<InteractiveItem> item = new AtomicReference<>();
        boolean isInteractiveItem = items.stream()
                .anyMatch(interativeItem -> {
                    item.set(interativeItem);

                    return interativeItem.getItem().isSimilar(e.getCurrentItem());
                });

        if (isInteractiveItem) {
            e.setCancelled(true);
        }

        listeners.forEach(listener -> listener.onInventoryClick(player, item.get()));
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
            player.getInventory().setItem(interactiveItem.getSlot(), interactiveItem.getItem());
        }
    }

    /**
     * Salir del modo y devolver
     * inventario anterior al jugador.
     */

    public void exit(Player player) {
        if (previousInventory == null) { // Se perdió tras una posible serialización.
            player.getInventory().clear();
        } else {
            for (int i = 0; i < previousInventory.length; i++) {
                player.getInventory().setItem(i, previousInventory[i]);
            }
        }

        CUtils.unregisterEvents(this);

        listeners.forEach(listener -> listener.onExit(player));
    }

    public void addListener(InteractiveListener listener) {
        listeners.add(listener);
    }

    public void removeListener(InteractiveListener listener) {
        listeners.remove(listener);
    }

    private boolean isNotSamePlayer(Player entity) {
        return !this.player.equals(entity.getUniqueId());
    }

}
