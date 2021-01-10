package net.oldust.core.staff.logs;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import net.minecraft.server.v1_16_R3.AdvancementFrameType;
import net.oldust.core.Core;
import net.oldust.core.internal.inventories.AbstractInventoryProvider;
import net.oldust.core.mysql.MySQLManager;
import net.oldust.core.utils.Async;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.GeoIPUtils;
import net.oldust.core.utils.ItemBuilder;
import net.oldust.core.utils.advancement.FakeAdvancement;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.sql.rowset.CachedRowSet;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class LogsInventory extends AbstractInventoryProvider {
    private static final String ITEM_NAME = "#fcba03 Log";
    private static final String INV_NAME = "Logs (%d)";
    private static final int LIMIT = 28;

    private final String nickname;
    private List<PlayerLog> logs;

    private int page;
    private boolean loading;

    private LogsInventory(Player player, String nickname, List<PlayerLog> logs, int page) {
        super(player);

        this.nickname = nickname;
        this.logs = logs;
        this.page = page;

        loading = false;
    }

    public LogsInventory(Player player, String nickname) {
        super(player);

        this.nickname = nickname;
        this.page = 0;

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
                            "#a6a6a6 Nickname: &f" + log.getPlayerName(),
                            "#a6a6a6 IP Address: &f" + log.getIp(),
                            "#a6a6a6 Joined: &f" + log.getJoin().toString(),
                            "#a6a6a6 Exited: &f" + log.getExit().toString(),
                            " ",
                            "#fcba03 Click to Geo-IP.")
                    ).build();

            items[i] = ClickableItem.of(item, (click) -> {
                CompletableFuture<GeoIPUtils.GeoResponse> future = CompletableFuture.supplyAsync(() -> {
                    GeoIPUtils.GeoResponse response = null;

                    try {
                        response = GeoIPUtils.gatherIpInfo(log.getIp());
                    } catch (IOException e) {
                        e.printStackTrace();
                        CUtils.msg(player, Lang.ERROR_COLOR + "An error occurred while sending the response to the Geo-IP API. An error stack trace was printed in the console.", LangSound.ERROR);
                    }

                    return response;
                });

                future.thenApplyAsync(response -> {
                    if (response == null) {
                        future.cancel(true);
                    }

                    return response;
                }).thenAccept(geoResponse -> CUtils.runSync(() -> {
                    FakeAdvancement respAdv = FakeAdvancement.builder()
                            .key(new NamespacedKey(Core.getInstance(), "geo/ip"))
                            .title("#a6a6a6City: &f" + geoResponse.getCity() +
                                    "\n#a6a6a6Region: &f" + geoResponse.getRegionName() +
                                    "\n#a6a6a6Country: &f" + geoResponse.getCountryName())
                            .item("heart_of_the_sea")
                            .description("Geo-IP")
                            .frame(AdvancementFrameType.TASK)
                            .announceToChat(false)
                            .build();

                    respAdv.show(player, true, true);
                }));
            });

        }

        pagination.setItems(items);
        pagination.setItemsPerPage(28);

        contents.fillBorders(EMPTY);

        contents.set(5, 8, ClickableItem.of(NAVIGATOR,
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
                .provider(new LogsInventory(player, nickname, retrieveLogs(LIMIT, page * LIMIT), page))
                .manager(Core.getInstance().getInventoryManager())
                .build();
    }

    @Async
    public List<PlayerLog> retrieveLogs(int limit, int offset) {
        CUtils.warnSyncCall();

        List<PlayerLog> logs = new ArrayList<>();

        CachedRowSet set = MySQLManager.query(
                "SELECT id, nickname, INET_NTOA(ip) AS ip, `exit`, `join` " +
                        "FROM dustplayers.logs WHERE nickname = ? " +
                        "ORDER BY `exit` DESC " +
                        "LIMIT " + limit + " OFFSET " + offset + ";", nickname
        );

        try {
            while (set.next()) {
                int id = set.getInt("id");
                String playerName = set.getString("nickname");
                String ip = set.getString("ip");
                Date join = new Date(set.getLong("join"));

                long exitEpoch = set.getLong("exit");

                if (exitEpoch == 0) { // When 0, exit is null, therefore player is connected.
                    continue;
                }

                Date exit = new Date(exitEpoch);

                logs.add(new PlayerLog(id, playerName, ip, join, exit));
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
            CUtils.msg(player, Lang.ERROR_COLOR + "Please wait until the desired page is loaded.", LangSound.ERROR);

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
                .thenAcceptAsync(inv -> CUtils.runSync(() -> inv.open(player)));

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5F, Float.MAX_VALUE);
    }

}
