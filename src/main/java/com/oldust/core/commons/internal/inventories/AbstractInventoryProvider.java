package com.oldust.core.commons.internal.inventories;

import com.oldust.core.utils.ItemBuilder;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

@RequiredArgsConstructor
public abstract class AbstractInventoryProvider implements InventoryProvider {
    public static final ClickableItem EMPTY = ClickableItem.empty(new ItemStack(Material.AIR));
    public static final ItemStack NAVIGATOR = new ItemBuilder(Material.ARROW)
            .setDisplayName("#fcba03 Navigator")
            .setLore(Arrays.asList(
                    " ",
                    "#a6a6a6 (#fcba03 ->#a6a6a6) &fRight click",
                    "#a6a6a6 (#fcba03 <-#a6a6a6) &fReft click",
                    "")
            ).build();

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
        build().open(player);
    }

    public SmartInventory build() {
        return buildInventory();
    }

}