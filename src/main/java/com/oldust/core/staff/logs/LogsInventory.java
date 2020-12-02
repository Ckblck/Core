package com.oldust.core.staff.logs;

import com.oldust.core.Core;
import com.oldust.core.inventories.AbstractInventoryProvider;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.ItemBuilder;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.advancement.FakeAdvancement;
import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.AdvancementFrameType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LogsInventory extends AbstractInventoryProvider {
    private static final ClickableItem EMPTY = ClickableItem.empty(new ItemStack(Material.AIR));

    private static final String ITEM_NAME = ChatColor.of("#fcba03") + "Log";
    private static final String INV_NAME = "Logs (%d)";
    private static final int LIMIT = 28;

    private final UUID search;
    private List<PlayerLog> logs;
    private int page;
    private boolean loading;

    public LogsInventory(Player player, UUID search, List<PlayerLog> logs, int page) {
        super(player);

        this.search = search;
        this.logs = logs;
        this.page = page;

        loading = false;
    }

    public LogsInventory(Player player, UUID search) {
        super(player);

        this.search = search;
        this.page = 0;

        CUtils.runSync(() -> {
            FakeAdvancement adv = FakeAdvancement.builder()
                    .key(new NamespacedKey("oldustcore", "sub/folder"))
                    .title("Test")
                    .item("heart_of_the_sea")
                    .description("Test")
                    .frame(AdvancementFrameType.GOAL)
                    .announceToChat(false)
                    .build();

            adv.show(player, true);
        });

        CompletableFuture.supplyAsync(this::build)
                .thenAccept(inv -> CUtils.runSync(() -> inv.open(player)));
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        ClickableItem[] items = new ClickableItem[logs.size()];
        Pagination pagination = contents.pagination();

        for (int i = 0; i < logs.size(); i++) {
            PlayerLog log = logs.get(i);

            ItemStack item = new ItemBuilder(Material.PAPER)
                    .setDisplayName(ITEM_NAME)
                    .setLore(Arrays.asList(" ",
                            "#a6a6a6 IP Address: &f" + log.getIp(),
                            "#a6a6a6 Joined: &f" + log.getJoin().toString(),
                            "#a6a6a6 Exited: &f" + log.getExit().toString(),
                            " ",
                            "#fcba03 Click to Geo-IP.")
                    ).build();

            items[i] = ClickableItem.of(item, (click) -> {
            });
        }

        pagination.setItems(items);
        pagination.setItemsPerPage(28);

        contents.fillBorders(EMPTY);

        contents.set(5, 8, ClickableItem.of(new ItemStack(Material.ARROW),
                (click) -> handleClick(pagination, click)));

        SlotIterator iterator = contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 1);
        iterator.allowOverride(false);

        pagination.addToIterator(iterator);
    }

    @Override
    public void update(Player player, InventoryContents inventoryContents) {
        // Not used.
    }

    @Override
    protected SmartInventory buildInventory() {
        loading = true;

        return SmartInventory.builder()
                .title(String.format(INV_NAME, page + 1))
                .size(6, 9)
                .provider(new LogsInventory(player, search, retrieveLogs(search, LIMIT, page * LIMIT), page))
                .manager(Core.getInstance().getInventoryManager())
                .build();
    }

    public List<PlayerLog> retrieveLogs(UUID from, int limit, int offset) {
        CUtils.warnSyncCall();

        List<PlayerLog> logs = new ArrayList<>();

        CachedRowSet set = MySQLManager.query(
                "SELECT id, uuid, INET_NTOA(ip) AS ip, `exit`, `join` " +
                        "FROM dustplayers.logs WHERE uuid = ? " +
                        "ORDER BY `exit` DESC " +
                        "LIMIT " + limit + " OFFSET " + offset + ";", from.toString()
        );

        try {
            while (set.next()) {
                int id = set.getInt("id");
                UUID uuid = UUID.fromString(set.getString("uuid"));
                String ip = set.getString("ip");
                Date join = new Date(set.getLong("join"));
                Date exit = new Date(set.getLong("exit"));

                logs.add(new PlayerLog(id, uuid, ip, join, exit));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }

    @Override
    public void handleClick(Pagination pagination, InventoryClickEvent click) {
        if (loading) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, Float.MIN_VALUE);
            CUtils.msg(player, Lang.ERROR_COLOR + "Please wait until the desired page is loaded.");

            return;
        }

        if (click.getClick().isRightClick()) {
            page++;
        } else {
            if (page == 0) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5F, Float.MIN_VALUE);

                return;
            }

            page--;
        }

        CompletableFuture.supplyAsync(this::build)
                .thenAccept(inv -> CUtils.runSync(() -> inv.open(player)));

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, Float.MAX_VALUE);
    }

}
