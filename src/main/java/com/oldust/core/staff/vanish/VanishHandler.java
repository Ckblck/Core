package com.oldust.core.staff.vanish;

import com.oldust.core.Core;
import com.oldust.core.commons.EventsProvider;
import com.oldust.core.commons.Operation;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Encargado de establecerle un jugador
 * con Vanish, y mantenerlo durante
 * el traspaso de servidores.
 */

public class VanishHandler implements Listener {
    private final Set<UUID> vanished = new HashSet<>();

    public VanishHandler() {
        for (Player player : PlayerUtils.getPlayers()) {
            WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());
            boolean vanished = database.contains(PlayerDatabaseKeys.VANISH);

            if (vanished) {
                vanish(player);
            }

        }

        joinEvent();
    }

    public void vanish(Player player) {
        Collection<? extends Player> list = PlayerUtils.getPlayers();

        for (Player otherPlayer : list) {
            otherPlayer.hidePlayer(Core.getInstance(), player);
        }

        vanished.add(player.getUniqueId());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !vanished.contains(player.getUniqueId())) cancel();

                player.sendActionBar(CUtils.color("#a7ab8c You are currently #e0895a vanished#a7ab8c."));
            }
        }.runTaskTimerAsynchronously(Core.getInstance(), 0, 20);

    }

    public void unvanish(Player player) {
        Collection<? extends Player> list = PlayerUtils.getPlayers();

        for (Player otherPlayer : list) {
            otherPlayer.showPlayer(Core.getInstance(), player);
        }

        vanished.remove(player.getUniqueId());
    }

    public void joinEvent() {
        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(PlayerJoinEvent.class, new Operation<PlayerJoinEvent>((join, db) -> {
            boolean vanished = db.contains(PlayerDatabaseKeys.VANISH);

            if (vanished) {
                vanish(join.getPlayer());
            }

            this.vanished.stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .forEach(vanishedPl -> join.getPlayer().hidePlayer(Core.getInstance(), vanishedPl));

        }));
    }

    public void switchState(Player player) {
        boolean vanished = this.vanished.contains(player.getUniqueId());

        if (vanished) {
            unvanish(player);
        } else {
            vanish(player);
        }

    }

}
