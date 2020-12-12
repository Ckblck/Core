package com.oldust.core.ranks.permission;

import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public class RankPermissions {
    private final PlayerRank rank;

    private final Map<String, Boolean> permissions = new HashMap<>();
    private final PermissionsManager manager;

    @Setter
    private PlayerRank parent;

    /**
     * Establecerle al jugador todos sus permisos pertinentes.
     * Si cambió de servidor, los descargará de redis, caso contrario de la base de datos.
     * Si el jugador tiene permisos antes de ejecutar este método, serán borrados.
     * Se establecerán los permisos propios del jugador, del rango y parents.
     * Se guardará en la base de datos del jugador SOLAMENTE sus permisos personales.
     *
     * @param player          jugador el cual preparar
     * @param playersDatabase database del jugador
     */

    public void applyToPlayer(Player player, WrappedPlayerDatabase playersDatabase) {
        HashMap<String, Boolean> permissionsToApply = new HashMap<>();
        boolean hasPersonalPermissions = playersDatabase.contains(PlayerDatabaseKeys.PERSONAL_PERMISSIONS);

        if (!hasPersonalPermissions) { // El jugador entró por primera vez a la network, descargamos.
            CompletableFuture<CachedRowSet> future = new CompletableFuture<>();

            future.thenAcceptAsync(set -> {
                try {
                    while (set.next()) {
                        String permission = set.getString("permission");
                        boolean enabled = set.getBoolean("enabled");

                        permissionsToApply.put(permission, enabled);
                    }

                    playersDatabase.put(PlayerDatabaseKeys.PERSONAL_PERMISSIONS, permissionsToApply); // Guardamos en redis sus permisos personales.
                    PlayerManager.getInstance().saveDatabase(playersDatabase);

                    addRankPermissions(permissionsToApply); // Aplicamos al Map los permisos del rango.

                    BukkitCompat.setPermissions(player, permissionsToApply);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }).exceptionally(ex -> {
                ex.printStackTrace();

                return null;
            });

            MySQLManager.queryAsync("SELECT permission, enabled FROM dustpermissions.players_permissions WHERE uuid = ?;", future, player.getUniqueId().toString());
        } else { // Cambió de server, descargamos de redis y asignamos los permisos propios del rango.
            Map<String, Boolean> playerPermissions = playersDatabase.getValue(PlayerDatabaseKeys.PERSONAL_PERMISSIONS).asMap(String.class, Boolean.class);

            permissionsToApply.putAll(playerPermissions);
            addRankPermissions(permissionsToApply);

            BukkitCompat.setPermissions(player, permissionsToApply);
        }

    }

    /**
     * Agregar al map todos los permisos del rango
     * y sus parents.
     *
     * @param permissionsToApply mapa al cual se le agregarán permisos
     */

    private void addRankPermissions(HashMap<String, Boolean> permissionsToApply) {
        PlayerRank parent = this.parent;

        while (parent != null) {
            RankPermissions permissions = manager.getPermissions(parent);
            Map<String, Boolean> parentPermissions = permissions.getPermissions();

            parent = permissions.getParent();

            permissionsToApply.putAll(parentPermissions);
        }

        permissionsToApply.putAll(permissions);
    }

    public void updatePermission(String permission, boolean enabled) {
        permissions.put(permission, enabled);

        this.filter()
                .forEach(player -> BukkitCompat.setPermissions(player, Map.of(permission, enabled)));
    }

    public void removePermission(String permission) {
        permissions.remove(permission);

        recalculateAll();
    }

    public void updateParent(PlayerRank newParent) {
        parent = newParent;

        recalculateAll();
    }

    private void recalculateAll() {
        PlayerManager playerManager = PlayerManager.getInstance();

        this.filter()
                .forEach(player -> applyToPlayer(player, playerManager.getDatabase(player.getUniqueId())));
    }

    private Collection<Player> filter() {
        PlayerManager playerManager = PlayerManager.getInstance();

        return PlayerUtils.getPlayers().stream()
                .filter(player -> {
                    WrappedPlayerDatabase db = playerManager.getDatabase(player.getUniqueId());
                    PlayerRank playerRank = db.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

                    return playerRank == rank;
                }).collect(Collectors.toList());
    }

}
