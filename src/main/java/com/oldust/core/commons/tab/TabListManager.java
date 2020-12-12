package com.oldust.core.commons.tab;

import com.oldust.core.Core;
import com.oldust.core.commons.internal.EventsProvider;
import com.oldust.core.commons.internal.Operation;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabListManager {

    public TabListManager() {
        event();
    }

    private void event() {
        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(PlayerJoinEvent.class, new Operation<PlayerJoinEvent>((ev, db) -> {
            Player player = ev.getPlayer();
            PlayerRank rank = db.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

            setTabPrefix(rank, player);
            setTabExtras(player);
        }));

    }

    public void setTabExtras(Player player) {
        player.setPlayerListHeaderFooter("\n Test1 \n", "\n Test2 \n");
    }

    public void setTabPrefix(PlayerRank rank, Player player) {
        CUtils.runSync(() -> rank.setTabPrefix(player));
    }

}
