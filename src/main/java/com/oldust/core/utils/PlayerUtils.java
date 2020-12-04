package com.oldust.core.utils;

import com.oldust.core.Core;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.ranks.RankWithExpiration;
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

    public boolean isConnected(UUID uuid) {
        return getPlayers().stream()
                .anyMatch(player -> player.getUniqueId().equals(uuid));
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
