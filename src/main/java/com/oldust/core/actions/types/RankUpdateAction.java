package com.oldust.core.actions.types;

import com.oldust.core.actions.Action;
import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.commons.CommonsPlugin;
import com.oldust.core.commons.permission.PermissionsManager;
import com.oldust.core.inherited.plugins.InheritedPluginsManager;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
    protected void execute() {
        if (!PlayerUtils.isLocallyConnected(player)) return;

        Player player = Bukkit.getPlayer(this.player);
        PlayerManager manager = PlayerManager.getInstance();
        WrappedPlayerDatabase database = manager.getDatabase(this.player);
        PlayerRank rank = database.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

        assert player != null;

        database.put(PlayerDatabaseKeys.RANK, newRank);
        manager.update(database);

        PermissionsManager permissionsManager = InheritedPluginsManager.getPlugin(PermissionsManager.class);
        permissionsManager.setupPlayer(player); // Actualizamos sus permisos.

        CommonsPlugin commonsPlugin = InheritedPluginsManager.getPlugin(CommonsPlugin.class);
        commonsPlugin.getTabListManager().setTabPrefix(rank, player); // Actualizamos su nombre en el Tab
    }

}
