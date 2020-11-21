package com.oldust.core.actions.types;

import com.oldust.core.actions.Action;
import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.inherited.plugins.InheritedPluginsManager;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.ranks.permission.PermissionsManager;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.WrappedPlayerDatabase;
import org.bukkit.Bukkit;

import java.util.UUID;

public class RankUpdateAction extends Action<RankUpdateAction> {
    private final UUID player;
    private final PlayerRank newRank;

    public RankUpdateAction(UUID player, PlayerRank newRank) {
        super(ActionsReceiver.PREFIX);

        this.player = player;
        this.newRank = newRank;
    }

    @Override
    public void execute() {
        if (!PlayerUtils.isConnected(player)) return;

        PlayerManager manager = PlayerManager.getInstance();
        WrappedPlayerDatabase database = manager.getDatabase(player);

        database.put(PlayerDatabaseKeys.RANK, newRank);
        manager.update(database);

        PermissionsManager permissionsManager = InheritedPluginsManager.getPlugin(PermissionsManager.class);
        permissionsManager.setupPlayer(Bukkit.getPlayer(player)); // Actualizamos sus permisos.
    }

}
