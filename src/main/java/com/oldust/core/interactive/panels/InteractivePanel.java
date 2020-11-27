package com.oldust.core.interactive.panels;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;

import java.io.Serializable;
import java.util.*;

/**
 * Un panel interactivo es aquel que
 * ejecuta un código a partir de una interacción
 * con determinado Item.
 */

@Getter
public class InteractivePanel implements Serializable {
    private final UUID player;
    private final transient ItemStack[] previousInventory;
    private final transient List<InteractiveItem> items = new ArrayList<>();
    private final Set<InteractiveListener> listeners = new HashSet<>();

    public InteractivePanel(Player player) {
        this.player = player.getUniqueId();
        this.previousInventory = player.getInventory().getContents();
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

    protected void enter(Player player) {
        player.getInventory().clear();

        for (InteractiveItem interactiveItem : items) {
            player.getInventory().setItem(interactiveItem.getSlot(), interactiveItem.getItem());
        }
    }

    /**
     * Salir del modo y devolver
     * inventario anterior al jugador.
     */

    protected void exit(Player player) {
        if (previousInventory == null) { // Se perdió tras una posible serialización.
            player.getInventory().clear();
        } else {
            for (int i = 0; i < previousInventory.length; i++) {
                player.getInventory().setItem(i, previousInventory[i]);
            }
        }

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
