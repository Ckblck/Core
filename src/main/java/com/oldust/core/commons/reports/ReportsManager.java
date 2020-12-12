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

/**
 * Permite el fácil acceso a los reportes
 * y brinda sus métodos necesarios.
 */

@Async
public class ReportsManager {

    /**
     * Agregar un reporte a la base
     * de datos.
     *
     * @param report reporte a insertar
     */

    public void setReport(Report report) {
        CUtils.warnSyncCall();

        MySQLManager.update("INSERT INTO dustplayers.reports (reported, reporter, reason, date) VALUES (?, ?, ?, ?);",
                report.getReported(), report.getReporter(), report.getReason(), report.getDate());
    }

    /**
     * Remover un reporte de la base
     * de datos.
     *
     * @param reported nombre del jugador reportado
     *                 cuyos reportes se eliminarán
     */

    public void removeReport(String reported) {
        CUtils.warnSyncCall();

        MySQLManager.update("DELETE FROM dustplayers.reports WHERE reported = ?;",
                reported);
    }

    /**
     * Obtener todos los reportes
     * de todos los jugadores.
     *
     * @return un mapa cuya Key es el nombre del jugador reportado
     * y su Value una lista de todos los reportes que reúne,
     * ordenados de tal forma que el primero de la lista es el
     * primero reportado hacia el jugador.
     */

    public Map<String, List<Report>> fetchReports() {
        CUtils.warnSyncCall();

        Map<String, List<Report>> reports = new HashMap<>();
        CachedRowSet set = MySQLManager.query("SELECT reported, reporter, reason, date FROM dustplayers.reports ORDER BY date ASC;");

        try {
            while (set.next()) {
                String reported = set.getString("reported");
                String reporter = set.getString("reporter");
                String reason = set.getString("reason");
                Timestamp date = set.getTimestamp("date");

                Report report = new Report(reported, reporter, reason, date);

                reports.putIfAbsent(reported, new ArrayList<>());

                reports.compute(reported, (key, value) -> {
                    assert value != null;
                    value.add(report);

                    return value;
                });

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
