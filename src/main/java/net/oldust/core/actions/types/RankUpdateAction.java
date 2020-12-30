package net.oldust.core.actions.types;

import net.oldust.core.actions.Action;
import net.oldust.core.actions.ActionsReceiver;
import net.oldust.core.commons.CommonsPlugin;
import net.oldust.core.inherited.plugins.InheritedPluginsManager;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Acción ejecutada al momento
 * de que el rango de un jugador cambia.
 * Esta acción es pusheada desde el Permissioner,
 * el cual se encarga de la administración de
 * rangos que expiran / se modifican.
 */

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
