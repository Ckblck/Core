package com.oldust.core.commons.reports;

import com.oldust.core.Core;
import com.oldust.core.commons.internal.inventories.AbstractInventoryProvider;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import org.bukkit.entity.Player;

public class ReportsInventory extends AbstractInventoryProvider {
    private static final String INV_NAME = "Reports";

    public ReportsInventory(Player player) {
        super(player);
    }

    @Override
    protected SmartInventory buildInventory() {
        return SmartInventory.builder()
                .title(INV_NAME)
                .size(6, 9)
                .provider(new ReportsInventory(player))
                .manager(Core.getInstance().getInventoryManager())
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {

    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }

}
