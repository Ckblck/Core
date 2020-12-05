package com.oldust.core.staff.punish.types;

import com.oldust.core.actions.types.KickPlayerAction;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.staff.punish.Punishment;
import com.oldust.core.staff.punish.PunishmentType;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.sync.JedisManager;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAmount;
import java.util.*;

public class BanPunishment implements Punishable {
    private static final PunishmentType TYPE = PunishmentType.BAN;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.ENGLISH);
    private static final String MESSAGE_STRUCTURE = CUtils.color(
            Lang.ERROR_COLOR +
                    "You have been banned!" + "\n\n" +
                    "#a6a6a6 Reason: &f%s \n" +
                    "#a6a6a6 Banned by: &f%s \n" +
                    "#a6a6a6 Expires: &f%s \n" +
                    "#a6a6a6 Ban ID: &f#%d \n\n" +
                    Lang.SUCCESS_COLOR_ALT + "You may appeal at:" + "\n" +
                    "#a6a6a6 https://oldust.com/appeal"
    ); // TODO: Site

    /**
     * Intenta agregar un ban a la base de datos.
     * Este método NO verifica si el jugador ya tiene
     * una sanción previa. Deberá comprobarse con {@link Punishable#hasActivePunishment(UUID)}
     *
     * @param punisherName nombre del jugador que sanciona
     * @param punishedName nombre del jugador a sancionar
     * @param duration     duración de la sanción, nulo para indicar permanencia
     * @param reason       razón de la sanción
     * @param banIp        true si la sanción es de IP
     * @return siempre true
     */

    @Override
    public boolean punish(String punisherName, String punishedName, @Nullable TemporalAmount duration, String reason, boolean banIp) {
        CUtils.warnSyncCall();

        UUID punishedUuid = PlayerUtils.getUUIDByName(punishedName);
        boolean noExpiration = duration == null;

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Timestamp expiration = noExpiration
                ? null
                : Timestamp.valueOf(currentTimestamp.toLocalDateTime().plus(duration));

        String ipAddress = (banIp) ? PlayerUtils.getIPAddress(punishedUuid) : null;

        CachedRowSet generatedKey = MySQLManager.updateWithGeneratedKeys("INSERT INTO dustbans.current_bans (uuid, reason, banned_by, ban_date, expiration, ip) VALUES (?, ?, ?, ?, ?, INET_ATON(?));",
                punishedUuid.toString(), reason, punisherName, currentTimestamp, expiration, ipAddress);

        int id = -1;

        try {
            if (generatedKey.next()) {
                id = generatedKey.getInt(1); // Obtenemos el ID del ban.
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (PlayerUtils.isConnected(punishedUuid)) {
            Punishment punishment = new Punishment(id, TYPE, punishedUuid, punisherName, reason, currentTimestamp, expiration, ipAddress);
            reason = getPunishmentMessage(punishment);

            new KickPlayerAction(punishedName, reason).push(JedisManager.getInstance().getPool());
        }

        return true;
    }

    /**
     * Remover una sanción de un jugador de la base de datos,
     * de manera que sea capaz de entrar a Oldust.
     * Esto NO registrará la sanción como expirada,
     * para ello, {@link #registerFinishedBan(ExpiredBan)}.
     *
     * @param punishedName nombre del jugador a remover la sanción
     * @return true si se ha removido, false si no tiene sanción
     */

    @Override
    public boolean removePunishment(String punishedName) {
        CUtils.warnSyncCall();

        UUID punishedUuid = PlayerUtils.getUUIDByName(punishedName);
        int update = MySQLManager.update("DELETE FROM dustbans.current_bans WHERE uuid = ?;", punishedUuid.toString());

        return update > 0;
    }

    /**
     * Registrar una sanción como expirada
     * en la tabla 'dustbans.expired_bans'.
     * Este método se usa, por ejemplo, al momento
     * de dar unban, o cuando {@link com.oldust.core.BungeeCore} limpia
     * los bans que expiraron.
     *
     * @param ban instancia de {@link ExpiredBan} que se
     *            insertará
     */

    public void registerFinishedBan(ExpiredBan ban) {
        CUtils.warnSyncCall();

        int id = ban.getPunishmentId();
        UUID uuid = ban.getPunishedUuid();
        String reason = ban.getReason();
        String punisherName = ban.getPunisherName();
        Timestamp date = ban.getDate();
        Timestamp expiration = ban.getExpiration();
        String unbannedBy = ban.getUnbannedBy();
        Timestamp unbannedAt = ban.getUnbannedAt();

        MySQLManager.update("INSERT INTO dustbans.expired_bans " +
                        "(ban_id, uuid, reason, banned_by, ban_date, expiration, unbanned_by, unbanned_at)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
                id, uuid.toString(), reason, punisherName, date, expiration, unbannedBy, unbannedAt);
    }

    @Override
    public boolean hasActivePunishment(UUID punishedUuid) {
        return currentPunishment(punishedUuid).isPresent();
    }

    public Optional<Punishment> currentPunishment(UUID punishedUuid, String ipAddress) {
        CUtils.warnSyncCall();

        CachedRowSet set = MySQLManager.query("SELECT id, reason, banned_by, ban_date, expiration," +
                " INET_NTOA(ip) AS ip" +
                " FROM dustbans.current_bans WHERE uuid = ? OR ip = INET_ATON(?);", punishedUuid.toString(), ipAddress
        );

        try {
            if (set.next()) {
                int id = set.getInt("id");
                String reason = set.getString("reason");
                String punisherName = set.getString("banned_by");
                Timestamp banDate = set.getTimestamp("ban_date");
                Timestamp expiration = set.getTimestamp("expiration");

                Punishment punishment = new Punishment(id, TYPE, punishedUuid, punisherName,
                        reason, banDate, expiration, ipAddress
                );

                return Optional.of(punishment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<Punishment> currentPunishment(UUID punishedUuid) {
        String address = PlayerUtils.getIPAddress(punishedUuid);

        return currentPunishment(punishedUuid, address);
    }

    @Override
    public String getPunishmentMessage(Punishment punishment) {
        String reason = punishment.getReason();
        String punisherName = punishment.getPunisherName();
        Timestamp expiration = punishment.getExpiration();
        int id = punishment.getPunishmentId();
        String expires;

        if (expiration == null) {
            expires = Lang.ERROR_COLOR + "never";
        } else {
            expires = DATE_FORMAT.format(expiration);
        }

        return String.format(MESSAGE_STRUCTURE, reason, punisherName, expires, id);
    }

    /**
     * Obtiene una lista ordenada (últimas sanciones primeras)
     * de las sanciones que están MARCADAS como expiradas en la tabla 'dustbans.expired_bans'.
     * Este método NO obtendrá las sanciones que están listas para que expiren.
     *
     * @param playerName nombre del jugador
     * @return lista de sanciones que el jugador ha tenido
     */

    public List<ExpiredBan> fetchExpiredBans(String playerName) {
        CUtils.warnSyncCall();

        List<ExpiredBan> expiredBans = new ArrayList<>();
        UUID uuid = PlayerUtils.getUUIDByName(playerName);

        CachedRowSet set = MySQLManager.query("SELECT * FROM dustbans.expired_bans WHERE uuid = ? ORDER BY ban_date DESC;", uuid.toString());

        try {
            while (set.next()) {
                int id = set.getInt("id");
                String punisherName = set.getString("banned_by");
                String reason = set.getString("reason");
                Timestamp date = set.getTimestamp("ban_date");
                Timestamp expiration = set.getTimestamp("expiration");
                String unbannedBy = set.getString("unbanned_by");
                Timestamp unbannedAt = set.getTimestamp("unbanned_at");

                ExpiredBan expiredBan = new ExpiredBan(id, uuid, punisherName, reason, date, expiration, null, unbannedBy, unbannedAt);

                expiredBans.add(expiredBan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return expiredBans; // TODO Offset for pagination? GUI?
    }

    @Getter
    public static class ExpiredBan extends Punishment {
        private final String unbannedBy;
        private final Timestamp unbannedAt;

        public ExpiredBan(Punishment punishment, @Nullable String unbannedBy, @Nullable Timestamp unbannedAt) {
            super(punishment.getPunishmentId(), BanPunishment.TYPE, punishment.getPunishedUuid(),
                    punishment.getPunisherName(), punishment.getReason(), punishment.getDate(),
                    punishment.getExpiration(), punishment.getIp());

            this.unbannedBy = unbannedBy;
            this.unbannedAt = unbannedAt;
        }

        public ExpiredBan(int banId, UUID punishedUuid, String punisherName,
                          String reason, Timestamp date, @Nullable Timestamp expiration,
                          @Nullable String ipAddress, @Nullable String unbannedBy, @Nullable Timestamp unbannedAt) {
            super(banId, TYPE, punishedUuid, punisherName, reason, date, expiration, ipAddress);

            this.unbannedBy = unbannedBy;
            this.unbannedAt = unbannedAt;
        }
    }

}
