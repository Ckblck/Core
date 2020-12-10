package com.oldust.core.utils;

import com.oldust.core.Core;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.ranks.RankWithExpiration;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.sql.rowset.CachedRowSet;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@UtilityClass
public class PlayerUtils {

    public Collection<? extends Player> getPlayers() {
        return Bukkit.getOnlinePlayers();
    }

    /**
     * Comprobar si un nombre está registrado
     * en la base de datos.
     *
     * @param nickname nombre a comprobar
     * @return true si está almacenado en la base de datos
     */

    public boolean nicknameExistsDB(String nickname) {
        CUtils.warnSyncCall();

        CachedRowSet set = MySQLManager.query("SELECT EXISTS(SELECT * FROM dustplayers.data WHERE nickname = ?);",
                nickname);

        try {
            if (set.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Comprobar si un jugador está conectado
     * en el servidor en cuestión.
     *
     * @param uuid UUID del jugador
     * @return true si está conectado en el servidor local (NO Network-Wide).
     */

    public boolean isLocallyConnected(UUID uuid) {
        return getPlayers().stream()
                .anyMatch(player -> player.getUniqueId().equals(uuid));
    }

    /**
     * Obtiene el nombre de un jugador a partir de una IP.
     *
     * @param ipAddress IP pública del jugador
     * @return nickname del jugador, o null si no se encontró
     */

    public String getPlayerNameByIp(String ipAddress) {
        CUtils.warnSyncCall();

        CachedRowSet set = MySQLManager.query("SELECT nickname FROM dustplayers.data WHERE ip = INET_ATON(?);",
                ipAddress);

        try {
            if (set.next()) {
                return set.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Obtiene la UUID de un jugador a partir de su IP.
     *
     * @param ipAddress IP del jugador.
     * @return UUID del jugador, o null si no se encontró
     */

    public UUID getUUIDByIp(String ipAddress) {
        CUtils.warnSyncCall();

        CachedRowSet set = MySQLManager.query("SELECT uuid FROM dustplayers.data WHERE ip = INET_ATON(?);",
                ipAddress);

        try {
            if (set.next()) {
                return UUID.fromString(set.getString("uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Obtiene la dirección IP de un jugador a partir
     * de su UUID.
     *
     * @param uuid UUID del jugador
     * @return IP en forma de string, o null si no se encontró
     */

    public String getIpAddress(UUID uuid) {
        CUtils.warnSyncCall();

        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(uuid);

        if (database != null) {
            return database.getValue(PlayerDatabaseKeys.PLAYER_IP_ADDRESS).asString();
        }

        return getIpAddressDB(uuid);
    }

    public String getIpAddressDB(UUID uuid) {
        CUtils.warnSyncCall();

        CachedRowSet set = MySQLManager.query("SELECT INET_NTOA(ip) AS ip FROM dustplayers.data WHERE uuid = ?;",
                uuid.toString());

        try {
            if (set.next()) {
                return set.getString("ip");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public UUID getUUIDByName(String playerName) {
        CUtils.warnSyncCall();

        CachedRowSet query = MySQLManager.query("SELECT uuid FROM dustplayers.data WHERE nickname = ?;", playerName);
        UUID uuid = null;

        try {
            if (query.next()) {
                uuid = UUID.fromString(query.getString("uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return uuid;
    }

    /**
     * Obtener todos los rangos que un jugador
     * posee. Un {@link RankWithExpiration} estará primero que otro
     * SI su prioridad es mayor. Esto significa, que si usamos {@link List#get(int)}
     * obtendremos el rango de mayor prioridad (el que posee en ese momento).
     *
     * @param uuid uuid del jugador
     * @return lista ordenada de rangos con / sin expiración de un jugador
     */

    public List<RankWithExpiration> getRanks(String uuid) {
        CachedRowSet query = MySQLManager.query("SELECT * FROM dustplayers.rangos WHERE uuid = ?;", uuid);
        List<RankWithExpiration> playerRanks = new ArrayList<>();

        try {
            while (query.next()) {
                PlayerRank rank = PlayerRank.getById(query.getInt("rank"));
                Timestamp expiresAt = query.getTimestamp("expiration");

                playerRanks.add(new RankWithExpiration(rank, expiresAt));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        playerRanks.sort(Comparator.comparingInt(rank -> ((RankWithExpiration) rank).getRank().getPriority()).reversed());

        return playerRanks;
    }

    public void sendToServer(Player player, String server) {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(b)) {

            out.writeUTF("Connect");
            out.writeUTF(server);

            player.sendPluginMessage(Core.getInstance(), "BungeeCord", b.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
