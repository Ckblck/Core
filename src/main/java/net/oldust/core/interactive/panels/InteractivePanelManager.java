package net.oldust.core.interactive.panels;

import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.commons.internal.EventsProvider;
import net.oldust.core.commons.internal.Operation;
import net.oldust.core.utils.CUtils;
import org.bukkit.entity.Entity;
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

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class InteractivePanelManager implements Listener {
    @Getter
    private static InteractivePanelManager instance;

    private final Map<UUID, InteractivePanel> panels = new HashMap<>();

    public InteractivePanelManager() {
        instance = this;

        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(PlayerQuitEvent.class, new Operation<PlayerQuitEvent>((quit, db) -> {
            Player player = quit.getPlayer();

            if (hasNotPanel(player)) return;

            exitPanel(player);
        }));

        CUtils.registerEvents(this);
    }

    public boolean setPanel(Player player, InteractivePanel panel) {
        if (panels.containsKey(player.getUniqueId())) return false;

        panel.enter(player);
        panels.put(player.getUniqueId(), panel);

        return true;
    }

    public void exitPanel(Player player) {
        InteractivePanel panel = panels.remove(player.getUniqueId());

        if (panel == null) return;

        panel.exit(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onDeath(PlayerDeathEvent e) {
        if (hasNotPanel(e.getEntity())) return;

        InteractivePanel panel = panels.get(e.getEntity().getUniqueId());

        e.getDrops().clear();
        e.getDrops().addAll(Arrays.asList(panel.getPreviousInventory()));
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent e) {
        if (hasNotPanel(e.getPlayer())) return;

        e.setCancelled(true);
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.getItem() == null || hasNotPanel(player)) return;

        InteractivePanel panel = panels.get(player.getUniqueId());
        List<InteractiveItem> items = panel.getItems();

        e.setCancelled(true);

        items.stream()
                .filter(interactiveItem -> interactiveItem.getItem().isSimilar(e.getItem()))
                .findAny()
                .ifPresent(interactiveItem -> {
                    Set<InteractiveListener> listeners = panel.getListeners();

                    interactiveItem.getConsumer().accept(e);
                    listeners.forEach(listener -> listener.onInteract(player, interactiveItem));
                });
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent e) {
        if (hasNotPanel(e.getPlayer())) return;

        InteractivePanel panel = panels.get(e.getPlayer().getUniqueId());
        List<InteractiveItem> items = panel.getItems();

        boolean isInteractiveItem = items.stream()
                .anyMatch(interativeItem -> interativeItem.getItem().isSimilar(e.getItemDrop().getItemStack()));

        if (isInteractiveItem) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        if (hasNotPanel(e.getWhoClicked())) return;

        Player player = (Player) e.getWhoClicked();
        InteractivePanel panel = panels.get(e.getWhoClicked().getUniqueId());
        List<InteractiveItem> items = panel.getItems();

        AtomicReference<InteractiveItem> item = new AtomicReference<>();
        boolean isInteractiveItem = items.stream()
                .anyMatch(interativeItem -> {
                    item.set(interativeItem);

                    return interativeItem.getItem().isSimilar(e.getCurrentItem());
                });

        if (isInteractiveItem) {
            e.setCancelled(true);
        }

        panel.getListeners().forEach(listener -> listener.onInventoryClick(player, item.get()));
    }

    private boolean hasNotPanel(Entity player) {
        return !panels.containsKey(player.getUniqueId());
    }

}
