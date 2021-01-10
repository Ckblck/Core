package net.oldust.core.permission;

import net.oldust.core.Core;
import net.oldust.core.inherited.plugins.InheritedPlugin;
import net.oldust.core.inherited.plugins.Plugin;
import net.oldust.core.internal.provider.EventsProvider;
import net.oldust.core.mysql.MySQLManager;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Plugin encargado de
 * establecer al jugador los permisos
 * personales y los de su rango.
 */

@InheritedPlugin(name = "Permisos")
public class PermissionsManager extends Plugin {
    private final Map<PlayerRank, RankPermissions> rankPermissions = new EnumMap<>(PlayerRank.class);

    @Override
    public void onEnable() {
        loadRankPermissions();

        onJoin(); // Register the event.
    }

    private void loadRankPermissions() {
        for (PlayerRank rank : PlayerRank.values()) {
            rankPermissions.put(rank, new RankPermissions(rank, this));
        }

        CompletableFuture<CachedRowSet> future = new CompletableFuture<>();

        future.thenAcceptAsync(set -> {
            try {
                while (set.next()) {
                    PlayerRank rank = PlayerRank.getById(set.getInt("rank"));
                    String permission = set.getString("permission");
                    boolean enabled = set.getBoolean("enabled");

                    RankPermissions rankPermissions = this.rankPermissions.get(rank);
                    rankPermissions.getPermissions().put(permission, enabled);
                }

                CachedRowSet rowSet = MySQLManager.query("SELECT * FROM dustpermissions.ranks_parents;");

                while (rowSet.next()) {
                    PlayerRank rank = PlayerRank.getById(rowSet.getInt("rank"));
                    PlayerRank parent = PlayerRank.getById(rowSet.getInt("parent"));

                    if (rank == parent) continue;

                    RankPermissions rankPermissions = this.rankPermissions.get(rank);
                    rankPermissions.setParent(parent);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).exceptionally(ex -> {
            CUtils.inform("Permissions", Lang.ERROR_COLOR + "An error occurred while loading the permissions from the database.");
            ex.printStackTrace();

            return null;
        });

        MySQLManager.queryAsync("SELECT * FROM dustpermissions.ranks_permissions;", future);
    }

    @Override
    public void onDisable() {

    }

    public void onJoin() {
        EventsProvider eventsProvider = Core.getInstance().getEventsProvider();

        eventsProvider.newOperation(PlayerJoinEvent.class, (join, db) -> {
            Player player = join.getPlayer();

            setupPlayer(player);
        });

    }

    /**
     * Establece a un jugador los permisos pertinentes
     * de su rango. También puede utilizarse para actualizar
     * sus permisos al cambiar a otro rango.
     *
     * @param player jugador
     */

    public void setupPlayer(Player player) {
        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player);

        PlayerRank playerRank = database.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);
        RankPermissions rankPermissions = this.rankPermissions.get(playerRank);

        rankPermissions.applyToPlayer(player, database);
    }

    public RankPermissions getPermissions(PlayerRank rank) {
        return rankPermissions.get(rank);
    }

}
