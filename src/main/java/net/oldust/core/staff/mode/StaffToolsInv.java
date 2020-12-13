package net.oldust.core.staff.mode;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import net.oldust.core.Core;
import net.oldust.core.commons.internal.inventories.AbstractInventoryProvider;
import net.oldust.core.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class StaffToolsInv extends AbstractInventoryProvider {
    private static final String INV_NAME = "Tools";
    private final StaffMode mode;

    public StaffToolsInv(Player player, StaffMode mode) {
        super(player);

        this.mode = mode;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ClickableItem[] items = new ClickableItem[4];

        items[0] = ClickableItem.of(new ItemBuilder(Material.BELL).setDisplayName("#c0c23a « #31d4c3 Mute/Unmute Chat #c0c23a »").build(),
                (click) -> mode.muteChat(player));

        items[1] = ClickableItem.of(new ItemBuilder(Material.TRIPWIRE_HOOK).setDisplayName("#c0c23a « #31d4c3 Fake Chest #c0c23a »").build(),
                (click) -> mode.createChest(player));

        items[2] = ClickableItem.of(new ItemBuilder(Material.FEATHER).setDisplayName("#c0c23a « #31d4c3 Switch Mode #c0c23a »").build(),
                (click) -> mode.switchMode(player));

        items[3] = ClickableItem.of(new ItemBuilder(Material.TURTLE_EGG).setDisplayName("#c0c23a « #31d4c3 Night Vision #c0c23a »").build(),
                (click) -> mode.switchVision(player));

        contents.set(1, 1, items[0]);
        contents.set(1, 3, items[1]);
        contents.set(1, 5, items[2]);
        contents.set(1, 7, items[3]);
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
        // Not used.
    }

    @Override
    protected SmartInventory buildInventory() {
        return SmartInventory.builder()
                .title(INV_NAME)
                .size(3, 9)
                .provider(new StaffToolsInv(player, mode))
                .manager(Core.getInstance().getInventoryManager())
                .build();
    }

}