package com.oldust.core.ranks.permission;

import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.WrappedPlayerDatabase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@InheritedPlugin(name = "Permisos")
public class PermissionsManager extends Plugin implements Listener {
    private final Map<PlayerRank, RankPermissions> rankPermissions = new EnumMap<>(PlayerRank.class);

    @Override
    public void onEnable() {
        for (PlayerRank rank : PlayerRank.values()) {
            rankPermissions.put(rank, new RankPermissions(rank, this));
        }

        CompletableFuture<CachedRowSet> future = new CompletableFuture<>();

        future.thenAccept(set -> {
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
            CUtils.inform("Permisos", Lang.ERROR_COLOR + "Ocurrió un error al cargar los permisos desde la base de datos.");
            ex.printStackTrace();

            return null;
        });

        MySQLManager.queryAsync("SELECT * FROM dustpermissions.ranks_permissions;", future);
        CUtils.registerEvents(this);
    }

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        setupPlayer(player);
    }

    /**
     * Establece a un jugador los permisos pertinentes
     * de su rango. También puede utilizarse para actualizar
     * sus permisos al cambiar a otro rango.
     *
     * @param player jugador
     */

    public void setupPlayer(Player player) {
        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());

        PlayerRank playerRank = database.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);
        RankPermissions rankPermissions = this.rankPermissions.get(playerRank);

        rankPermissions.applyToPlayer(player, database);
    }

    public RankPermissions getPermissions(PlayerRank rank) {
        return rankPermissions.get(rank);
    }

}
