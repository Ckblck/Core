package com.oldust.core.utils;

import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.ranks.RankWithExpiration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class PlayerUtils {

    private PlayerUtils() {
    }

    public static Collection<? extends Player> getPlayers() {
        return Bukkit.getOnlinePlayers();
    }

    public static boolean isConnected(UUID uuid) {
        return getPlayers().stream()
                .anyMatch(player -> player.getUniqueId().equals(uuid));
    }

    public static UUID getUUIDByName(String playerName) {
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

    public static List<RankWithExpiration> getRanks(String uuid) {
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

}
