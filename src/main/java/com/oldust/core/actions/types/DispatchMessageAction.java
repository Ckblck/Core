package com.oldust.core.actions.types;

import com.oldust.core.Core;
import com.oldust.core.actions.Action;
import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.SerializablePredicate;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

import java.util.Collection;

public class DispatchMessageAction extends Action<DispatchMessageAction> {
    private final Channel channel;
    private final SerializablePredicate<PlayerRank> rankRequirement;
    private final String message;

    public DispatchMessageAction(Channel channel, SerializablePredicate<PlayerRank> rankRequirement, String message) {
        super(ActionsReceiver.PREFIX);

        this.channel = channel;
        this.rankRequirement = rankRequirement;
        this.message = message;
    }

    @Override
    protected void execute() {
        boolean shouldBroadcast = channel == Channel.NETWORK_WIDE || channel.serverName.equalsIgnoreCase(Core.getInstance().getServerName());

        if (!shouldBroadcast) return;
        Collection<? extends Player> players = PlayerUtils.getPlayers();

        for (Player player : players) {
            WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());
            PlayerRank playerRank = database.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

            boolean applies = rankRequirement.test(playerRank);

            if (applies) {
                CUtils.msg(player, message);
            }
        }

    }

    @RequiredArgsConstructor
    public enum Channel {
        SERVER_WIDE(Core.getInstance().getServerName()),
        NETWORK_WIDE("*");

        private final String serverName;
    }

}
