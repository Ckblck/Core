package net.oldust.core.commons.tab;

import net.md_5.bungee.api.ChatColor;
import net.oldust.core.Core;
import net.oldust.core.internal.provider.EventsProvider;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.utils.CUtils;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class TabListManager extends BukkitRunnable {
    private static final String HEADER = CUtils.color(
            "\n                &r#fcba03⚔ &r&lOLDUST #fcba03⚔                " +
                    "\n              &7{ip}             \n");
    private static final String FOOTER = CUtils.color(
            "\n                &rPlayers: &7{actual}&8/&7{max}             " +
                    "\n               &rPing: &a{ping}ms            \n" +
                    "\n &r&lSHOP: &ehttps://oldust.net/shop\n");

    private int cache;
    private String currentFooter;

    private boolean firstRun = true;
    private boolean reverse = false;
    private int hexNumber = 82;

    public TabListManager() {
        runTaskTimerAsynchronously(Core.getInstance(), 0, 1);

        event();
    }

    private void event() {
        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(PlayerJoinEvent.class, (ev, db) -> {
            Player player = ev.getPlayer();
            PlayerRank rank = db.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

            setTabPrefix(rank, player);
            setTabExtras(player);
        });

    }

    public void setTabExtras(Player player) {
        player.setPlayerListHeader(HEADER);

        player.setPlayerListFooter(getFooter(player));
    }

    public void setTabPrefix(PlayerRank rank, Player player) {
        rank.setTabPrefix(player);
    }

    @Override
    public void run() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        if (firstRun || cache == 100) { // Cada 5 segundos, actualizamos el ping y los jugadores
            cache = 0;
            firstRun = false;

            currentFooter = FOOTER.replace("{actual}", String.valueOf(players.size()));
            currentFooter = currentFooter.replace("{max}", String.valueOf(Bukkit.getMaxPlayers()));

            for (Player player : players) {
                String finalFooter = getFooter(player);

                player.setPlayerListFooter(finalFooter);
            }
        } else { // Gradiente
            if (hexNumber > 204) {
                reverse = true;
            }

            if (hexNumber < 82) {
                reverse = false;
            }

            if (reverse) {
                hexNumber -= 5;
            } else {
                hexNumber += 5;
            }

            String hex = String.format("#%02x%02x%02x", hexNumber, hexNumber, hexNumber);
            ChatColor color = ChatColor.of(hex);
            String header = HEADER.replace("{ip}", color + "mc.oldust.net");

            for (Player player : players) {
                player.setPlayerListHeader(header);
            }

        }

        cache++;
    }

    @NotNull
    private String getFooter(Player player) {
        int ping = player.spigot().getPing();

        return currentFooter.replace("{ping}", String.valueOf(ping));
    }

}
