package com.oldust.core.commons.reports;

import com.oldust.core.Core;
import com.oldust.core.commons.CommonsPlugin;
import com.oldust.core.commons.internal.inventories.AbstractInventoryProvider;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.ItemBuilder;
import com.oldust.core.utils.lang.Async;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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

            ClickableItem item = ClickableItem.of(buildItem(report), (click) -> {
                Bukkit.broadcastMessage(click.getAction().name() + " / " + click.getClick().name());
            });

            items[i] = item;
        }

        Pagination pagination = contents.pagination();

        pagination.setItems(items);
        pagination.setItemsPerPage(15);

        SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 3);

        iterator.blacklist(0, 0);
        iterator.blacklist(1, 0);
        iterator.blacklist(2, 0);
        iterator.blacklist(3, 0);
        iterator.blacklist(4, 0);
        iterator.blacklist(5, 0);

        iterator.blacklist(0, 1);
        iterator.blacklist(1, 1);
        iterator.blacklist(2, 1);
        iterator.blacklist(3, 1);
        iterator.blacklist(4, 1);
        iterator.blacklist(5, 1);

        iterator.blacklist(0, 2);
        iterator.blacklist(1, 2);
        iterator.blacklist(3, 2);
        iterator.blacklist(4, 2);
        iterator.blacklist(5, 2);

        iterator.blacklist(5, 0);
        iterator.blacklist(5, 1);
        iterator.blacklist(5, 2);
        iterator.blacklist(5, 3);
        iterator.blacklist(5, 4);
        iterator.blacklist(5, 5);
        iterator.blacklist(5, 6);
        iterator.blacklist(5, 7);
        iterator.blacklist(5, 8);

        iterator.blacklist(1, 6);
        iterator.blacklist(1, 7);
        iterator.blacklist(1, 8);
        iterator.blacklist(2, 7);
        iterator.blacklist(2, 8);
        iterator.blacklist(3, 7);
        iterator.blacklist(3, 8);

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
     */

    private ItemStack buildItem(List<Report> reports) {
        Report firstReport = reports.get(0);
        List<String> lore = new ArrayList<>();

        lore.add("#404040 " + firstReport.getReported() + " | #fcba03" + reports.size()
                + "#404040 time" + ((reports.size() == 1) ? "." : "s."));
        lore.add(" ");
        lore.add("#fcba03 Reports");

        for (int i = 0; i < reports.size(); i++) {
            Report report = reports.get(i);

            lore.add("#404040└─ &fReport #" + (i + 1));
            lore.add("#404040    └─ #a6a6a6 Reporter: &f" + report.getReporter());
            lore.add("#404040    └─ #a6a6a6 Reason: &f" + report.getReason());
            lore.add("#404040    └─ #a6a6a6 Date: &f" + FORMAT.format(report.getDate()));
        }

        lore.add(" ");
        lore.add("#404040(#a6a6a6L#404040) #fcba03 Teleport #404040 | #404040(#a6a6a6R#404040)#fcba03 Dismiss");

        return new ItemBuilder(Material.FLOWER_BANNER_PATTERN)
                .setDisplayName("#fcba03 Report")
                .setLore(lore)
                .addItemFlag(ItemFlag.HIDE_POTION_EFFECTS)
                .build();
    }

}
