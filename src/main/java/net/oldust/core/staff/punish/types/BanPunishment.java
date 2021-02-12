package net.oldust.core.staff.punish.types;

import com.google.common.base.Preconditions;
import net.oldust.core.BungeeCore;
import net.oldust.core.Core;
import net.oldust.core.actions.types.DispatchMessageAction;
import net.oldust.core.actions.types.KickPlayerAction;
import net.oldust.core.mysql.MySQLManager;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.punish.Punishment;
import net.oldust.core.staff.punish.PunishmentType;
import net.oldust.core.utils.Async;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.jedis.JedisManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BanPunishment implements Punishable<Punishment.ExpiredPunishment> {
    private static final String STAFF_ALERT_MESSAGE = CUtils.color("#ff443b[!] #80918a #fcba03 %s#80918a has banned #fcba03%s#80918a due to: %s.");
    private static final String MESSAGE_STRUCTURE = CUtils.color(
            Lang.ERROR_COLOR +
                    "You have been banned!" + "\n\n" +
                    "#a6a6a6 Reason: &f%s \n" +
                    "#a6a6a6 Banned by: &f%s \n" +
                    "#a6a6a6 Expires: &f%s \n" +
                    "#a6a6a6 Ban ID: &f#%d \n\n" +
                    Lang.SUCCESS_COLOR_ALT + "You may appeal at:" + "\n" +
                    "#a6a6a6 https://oldust.net/appeal"
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
     * @return false si el jugador no existe en la base de datos
     */

    @Override
    public boolean punish(String punisherName, String punishedName, @Nullable TemporalAmount duration, String reason, boolean banIp) {
        CUtils.warnSyncCall();

        UUID punishedUuid = PlayerUtils.getUUIDByName(punishedName);
        Preconditions.checkNotNull(punishedUuid);

        boolean noExpiration = duration == null;

        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        Timestamp expiration = noExpiration
                ? null
                : Timestamp.valueOf(currentTimestamp.toLocalDateTime().plus(duration));

        String ipAddress = (banIp) ? PlayerUtils.getIpAddress(punishedUuid) : null;

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

        if (Core.getInstance().getServerManager().isPlayerOnline(punishedName)) {
            Punishment punishment = new Punishment(id, PunishmentType.BAN, punishedUuid, punisherName, reason, currentTimestamp, expiration, ipAddress);
            String kickReason = getPunishmentMessage(punishment);

            new KickPlayerAction(punishedName, kickReason)
                    .push(JedisManager.getInstance().getPool());
        }

        String staffMessage = String.format(STAFF_ALERT_MESSAGE, punisherName, punishedName, ChatColor.stripColor(reason));

        new DispatchMessageAction(DispatchMessageAction.Channel.NETWORK_WIDE, db -> {
            PlayerRank rank = db.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

            return rank.isStaff();
        }, false, staffMessage, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F, 1F)
                .push(JedisManager.getInstance().getPool());

        return true;
    }

    /**
     * Remover una sanción de un jugador de la base de datos,
     * de manera que sea capaz de entrar a Oldust.
     * Esto NO registrará la sanción como expirada,
     * para ello, {@link #registerFinishedBan(Punishment.ExpiredPunishment)}.
     *
     * @param punishedName nombre del jugador a remover la sanción
     * @return true si se ha removido
     */

    @Override
    public boolean removePunishment(String punishedName) {
        CUtils.warnSyncCall();

        UUID punishedUuid = PlayerUtils.getUUIDByName(punishedName);
        Preconditions.checkNotNull(punishedUuid);

        int update = MySQLManager.update("DELETE FROM dustbans.current_bans WHERE uuid = ?;", punishedUuid.toString());

        return update > 0;
    }

    /**
     * Registrar una sanción como expirada
     * en la tabla 'dustbans.expired_bans'.
     * Este método se usa, por ejemplo, al momento
     * de dar unban, o cuando {@link BungeeCore} limpia
     * los bans que expiraron.
     *
     * @param ban instancia de {@link Punishment.ExpiredPunishment} que se
     *            insertará
     */

    @Async
    public void registerFinishedBan(Punishment.ExpiredPunishment ban) {
        CUtils.warnSyncCall();

        int id = ban.getPunishmentId();
        UUID uuid = ban.getPunishedUuid();
        String reason = ban.getReason();
        String punisherName = ban.getPunisherName();
        Timestamp date = ban.getDate();
        Timestamp expiration = ban.getExpiration();
        String unbannedBy = ban.getUnpunishedBy();
        Timestamp unbannedAt = ban.getUnpunishedAt();

        MySQLManager.update("INSERT INTO dustbans.expired_bans " +
                        "(ban_id, uuid, reason, banned_by, ban_date, expiration, unbanned_by, unbanned_at)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
                id, uuid.toString(), reason, punisherName, date, expiration, unbannedBy, unbannedAt);
    }

    @Override
    public boolean hasActivePunishment(UUID punishedUuid) {
        return currentPunishment(punishedUuid).isPresent();
    }

    @Async
    public Optional<Punishment> currentPunishment(UUID punishedUuid, String ipAddress) {
        CUtils.warnSyncCall();

        Preconditions.checkNotNull(punishedUuid);

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

                Punishment punishment = new Punishment(id, PunishmentType.BAN, punishedUuid, punisherName,
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
        CUtils.warnSyncCall();

        Preconditions.checkNotNull(punishedUuid);

        String address = PlayerUtils.getIpAddress(punishedUuid);

        return currentPunishment(punishedUuid, address);
    }

    @Override
    public String getPunishmentMessage(Punishment punishment) {
        String reason = punishment.getReason();
        String punisherName = punishment.getPunisherName();
        int id = punishment.getPunishmentId();
        String expires = punishment.formatExpiration();

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

    @Override
    public List<Punishment.ExpiredPunishment> fetchPunishments(String playerName) {
        CUtils.warnSyncCall();

        UUID uuid = PlayerUtils.getUUIDByName(playerName);

        Preconditions.checkNotNull(uuid);

        List<Punishment.ExpiredPunishment> expiredBans = new ArrayList<>();
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

                Punishment.ExpiredPunishment expiredBan = new Punishment.ExpiredPunishment(
                        id, PunishmentType.BAN, uuid, punisherName,
                        reason, date, expiration, null,
                        unbannedBy, unbannedAt
                );

                expiredBans.add(expiredBan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return expiredBans;
    }

}
