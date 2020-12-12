package com.oldust.core.commons.reports;

import com.oldust.core.Core;
import com.oldust.core.commons.CommonsPlugin;
import com.oldust.core.commons.internal.inventories.AbstractInventoryProvider;
import com.oldust.core.commons.internal.inventories.PageableItemStack;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.ItemBuilder;
import com.oldust.core.utils.lang.Async;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportsInventory extends AbstractInventoryProvider {
    private static final String INV_NAME = "Reports";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final CommonsPlugin plugin;

    private Map<String, List<Report>> reports;

    @Async
    public ReportsInventory(Player player, CommonsPlugin plugin) {
        super(player);

        this.plugin = plugin;

        SmartInventory inventory = build();
        CUtils.runSync(() -> inventory.open(player));
    }

    private ReportsInventory(Player player, CommonsPlugin plugin, Map<String, List<Report>> reports) {
        super(player);

        this.plugin = plugin;
        this.reports = reports;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ClickableItem[] items = new ClickableItem[reports.size()];
        List<List<Report>> reports = new ArrayList<>(this.reports.values());

        for (int i = 0; i < reports.size(); i++) {
            List<Report> report = reports.get(i);
            PageableItemStack pageableItem = buildItem(report);

            ClickableItem item = ClickableItem.of(pageableItem, (click) -> {
                InventoryAction action = click.getAction();
                int hotbarSlot = click.getHotbarButton() + 1;

                if (action != InventoryAction.HOTBAR_SWAP) return;

                if (hotbarSlot == 1) {
                    if (pageableItem.previousPage()) {
                        player.getOpenInventory().setItem(click.getSlot(), pageableItem);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, Float.MIN_VALUE);
                    }
                } else if (hotbarSlot == 2) {
                    if (pageableItem.nextPage()) {
                        player.getOpenInventory().setItem(click.getSlot(), pageableItem);
                    } else {
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, Float.MIN_VALUE);
                    }
                }

            });

            items[i] = item;
        }

        Pagination pagination = contents.pagination();

        pagination.setItems(items);
        pagination.setItemsPerPage(16);

        SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 3);

        for (int i = 0; i < 6; i++) {
            iterator.blacklist(i, 0);
            iterator.blacklist(i, 1);
        }

        for (int i = 0; i < 6; i++) {
            iterator.blacklist(i, 1);
        }

        for (int i = 0; i < 9; i++) {
            iterator.blacklist(5, i);
        }

        iterator.blacklist(1, 6);
        iterator.blacklist(1, 7);
        iterator.blacklist(1, 8);
        iterator.blacklist(2, 7);
        iterator.blacklist(2, 8);
        iterator.blacklist(3, 7);
        iterator.blacklist(3, 8);
        iterator.blacklist(4, 2);

        pagination.addToIterator(iterator);

        contents.set(4, 4, ClickableItem.of(NAVIGATOR, (click) -> {
            handleClick(pagination, click);
        }));
    }

    @Override
    public void update(Player player, InventoryContents contents) {
        // Not used.
    }

    @Override
    protected SmartInventory buildInventory() {
        Map<String, List<Report>> reports = plugin.getReportsManager().fetchReports();

        return SmartInventory.builder()
                .title(INV_NAME)
                .size(6, 9)
                .provider(new ReportsInventory(player, plugin, reports))
                .manager(Core.getInstance().getInventoryManager())
                .build();
    }

    /**
     * Construye el item a partir
     * de una lista de reportes que
     * un jugador ha recibido.
     *
     * @param reports lista de reportes
     *                del jugador
     * @return una implementación modificada de un {@link ItemStack}
     * que permite mostrar extensa información en su lore.
     */

    private PageableItemStack buildItem(List<Report> reports) {
        Report firstReport = reports.get(0);

        PageableItemStack stack = (PageableItemStack) new ItemBuilder(Material.FLOWER_BANNER_PATTERN, true)
                .setDisplayName("#fcba03 Report")
                .addItemFlag(ItemFlag.HIDE_POTION_EFFECTS)
                .build();

        List<String> lore = new ArrayList<>();

        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);

            lore.add("#404040 " + firstReport.getReported() + " | #fcba03" + reports.size()
                    + "#404040 time" + ((reports.size() == 1) ? "." : "s."));
            lore.add(" ");
            lore.add("#fcba03 Reports");

            lore.add("#404040└─ &fReport #" + (i + 1));
            lore.add("#404040    └─ #a6a6a6 Reporter: &f" + report.getReporter());
            lore.add("#404040    └─ #a6a6a6 Reason: &f" + report.getReason());
            lore.add("#404040    └─ #a6a6a6 Date: &f" + FORMAT.format(report.getDate()));

            lore.add(" ");
            lore.add("#404040(#a6a6a61#404040) " +
                    "#fcba03Left " +
                    "#404040| #404040(#a6a6a6L#404040)#fcba03 TP " +
                    "#404040| #404040(#a6a6a6R#404040)#fcba03 Dismiss " +
                    "#404040| #404040(#a6a6a62#404040)#fcba03 Right");

            stack.setPage(i, new ArrayList<>(lore));

            lore.clear();
        }

        return stack;
    }

}
