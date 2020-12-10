package com.oldust.core.commons.reports;

import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.utils.CUtils;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.entity.Player;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

                reports.add(new Report(id, reported, reporter, reason, date));
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
     * @param player jugador que reportó
     */

    public void registerReport(Player player) {
        CUtils.warnSyncCall();

        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player);

        //database.
    }

    //public boolean

}
