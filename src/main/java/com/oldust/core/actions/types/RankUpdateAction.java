package com.oldust.core.actions.types;

import com.oldust.core.actions.Action;
import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.WrappedPlayerDatabase;

import java.util.UUID;

public class RankUpdateAction extends Action<RankUpdateAction> {
    public static final String ACTION_FULL_NAME = ActionsReceiver.PREFIX;

    private final UUID player;
    private final PlayerRank newRank;

    public RankUpdateAction(UUID player, PlayerRank newRank) {
        super(ACTION_FULL_NAME);

        this.player = player;
        this.newRank = newRank;
    }

    @Override
    public void execute() {
        boolean playerConnected = PlayerUtils.getPlayers().stream()
                .anyMatch(onlinePlayer -> onlinePlayer.getUniqueId().equals(player));

        if (playerConnected) {
            PlayerManager manager = PlayerManager.getInstance();
            WrappedPlayerDatabase database = manager.getDatabase(player);

            database.put(PlayerDatabaseKeys.RANK, newRank);
            manager.update(database);
        }

    }

}
