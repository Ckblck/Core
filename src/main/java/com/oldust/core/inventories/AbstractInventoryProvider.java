package com.oldust.core.inventories;

import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import lombok.RequiredArgsConstructor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

@RequiredArgsConstructor
public abstract class AbstractInventoryProvider implements InventoryProvider {
    protected final Player player;

    public void handleClick(Pagination pagination, InventoryClickEvent click) {
        if (click.getClick().isRightClick()) {
            if (pagination.isLast()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, Float.MIN_VALUE);

                return;
            }

            buildInventory().open(player, pagination.next().getPage());
        } else {
            if (pagination.isFirst()) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, Float.MIN_VALUE);

                return;
            }

            buildInventory().open(player, pagination.previous().getPage());
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, Float.MAX_VALUE);
    }

    protected abstract SmartInventory buildInventory();

    public void open() {
        buildInventory().open(player);
    }

}