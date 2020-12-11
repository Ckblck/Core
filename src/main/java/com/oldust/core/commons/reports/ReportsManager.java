package com.oldust.core.commons.reports;

import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.lang.Async;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.Savable;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.entity.Player;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Async
public class ReportsManager {

    public void setReport(Report report) {
        CUtils.warnSyncCall();

        MySQLManager.update("INSERT INTO dustplayers.reports (reported, reporter, reason, date) VALUES (?, ?, ?, ?);",
                report.getReported(), report.getReporter(), report.getReason(), report.getDate());
    }

    public void removeReport(String reported) {
        CUtils.warnSyncCall();

        MySQLManager.update("DELETE FROM dustplayers.reports WHERE reported = ?;",
                reported);
    }

    public List<Report> fetchReports(String reported) {
        CUtils.warnSyncCall();

        List<Report> reports = new ArrayList<>();
        CachedRowSet set = MySQLManager.query("SELECT id, reporter, reason, date FROM dustplayers.reports WHERE reported = ?;",
                reported);

        try {
            while (set.next()) {
                int id = set.getInt("id");
                String reporter = set.getString("reporter");
                String reason = set.getString("reason");
                Timestamp date = set.getTimestamp("date");

                reports.add(new Report(reported, reporter, reason, date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reports;
    }

    /**
     * Registrar en la base de datos del jugador
     * el reporte hacia un usuario. Permitiendo así
     * denegar el intento de múltiple reporte.
     *
     * @param player   jugador que reportó
     * @param reported jugador reportado
     */

    public void registerReport(Player player, String reported) {
        CUtils.warnSyncCall();

        PlayerManager playerManager = PlayerManager.getInstance();
        WrappedPlayerDatabase database = playerManager.getDatabase(player);
        Optional<Savable.WrappedValue> reportedOpt = database.getValueOptional(PlayerDatabaseKeys.PLAYERS_REPORTED);

        reportedOpt
                .ifPresentOrElse((playersReported) -> {
                    Set<String> reporteds = playersReported.asSet(String.class);

                    reporteds.add(reported);
                }, () -> {
                    HashSet<String> reporteds = new HashSet<>();

                    reporteds.add(reported);

                    database.put(PlayerDatabaseKeys.PLAYERS_REPORTED, reporteds);
                });

        playerManager.update(database);
    }

    /**
     * Comprobar si un jugador ya ha previamente
     * reportado al mismo nombre.
     *
     * @param reporter      jugador que está reportando y a quién se comprobará
     * @param reportAttempt jugador que está siendo reportado
     * @return true si ya ha sido reportado por {@param reporter}.
     */

    public boolean hasReported(Player reporter, String reportAttempt) {
        CUtils.warnSyncCall();

        PlayerManager playerManager = PlayerManager.getInstance();
        WrappedPlayerDatabase database = playerManager.getDatabase(reporter);

        Optional<Savable.WrappedValue> optional = database.getValueOptional(PlayerDatabaseKeys.PLAYERS_REPORTED);

        if (optional.isPresent()) {
            Set<String> reporteds = optional.get().asSet(String.class);

            return reporteds.contains(reportAttempt);
        }

        return false;
    }

}
