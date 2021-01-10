package net.oldust.core.staff.punish.types;

import com.google.common.base.Preconditions;
import net.oldust.core.Core;
import net.oldust.core.actions.types.DispatchMessageAction;
import net.oldust.core.actions.types.SendPlayerMessageAction;
import net.oldust.core.mysql.MySQLManager;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.punish.Punishment;
import net.oldust.core.staff.punish.PunishmentType;
import net.oldust.core.utils.Async;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.JedisManager;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.ChatColor;
import org.bukkit.Sound;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAmount;
import java.util.*;

public class MutePunishment implements Punishable<Punishment.ExpiredPunishment> {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.ENGLISH);
    private static final String STAFF_ALERT_MESSAGE = CUtils.color("#ff443b[!] #80918a #fcba03 %s#80918a has muted #fcba03%s #80918adue to: %s.");
    private static final String MESSAGE = Lang.ERROR_COLOR
            + "\n-------------------"
            + "\nYou are currently muted due to: %s"
            + "\n * Mute ends: %s."
            + "\n * Mute-ID: #%d."
            + "\n-------------------"
            + "\n ";

    /**
     * Silencia a un jugador por un periodo
     * de tiempo y razón especificados.
     * Nota: este método NO comprueba si el jugador
     * ya está previamente silenciado.
     *
     * @param punisherName nombre del jugador que silencia al jugador
     * @param punishedName nombre del jugador al cual silenciar
     * @param duration     duración
     * @param reason       razón
     * @param banIp        indiferente, solo aplica para {@link BanPunishment}
     * @return indiferente
     */

    @Override
    public boolean punish(String punisherName, String punishedName, TemporalAmount duration, String reason, boolean banIp) {
        CUtils.warnSyncCall();

        UUID uuid = PlayerUtils.getUUIDByName(punishedName);
        Preconditions.checkNotNull(uuid);

        return punish(punisherName, punishedName, uuid, duration, reason, banIp);
    }

    /**
     * Silencia a un jugador por un periodo
     * de tiempo y razón especificados.
     * Nota: este método NO comprueba si el jugador
     * ya está previamente silenciado.
     * En caso de tener previamente la UUID del jugador,
     * se recomienda usar este método en vez de {@link #punish(String, String, TemporalAmount, String, boolean)}
     * para evitar una query.
     *
     * @param punisherName nombre de quien silencia
     * @param punishedName nombre del silenciado
     * @param punishedUuid uuid del silenciado
     * @param duration     duración del silencio
     * @param reason       razón
     * @param banIp        indiferente
     * @return indiferente
     */

    @Async
    public boolean punish(String punisherName, String punishedName, UUID punishedUuid, TemporalAmount duration, String reason, boolean banIp) {
        CUtils.warnSyncCall();

        Preconditions.checkNotNull(duration);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        Timestamp expires = Timestamp.valueOf(now.toLocalDateTime().plus(duration));

        CachedRowSet set = MySQLManager.updateWithGeneratedKeys("INSERT INTO dustbans.mutes (uuid, date, expiration, reason, muted_by) VALUES (?, ?, ?, ?, ?);",
                punishedUuid.toString(), now, expires, reason, punisherName);

        int id = -1;

        try {
            if (set.next()) {
                id = set.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (Core.getInstance().getServerManager().isPlayerOnline(punishedName)) {
            Punishment punishment = new Punishment(id, PunishmentType.MUTE, punishedUuid, punisherName, reason, now, expires, null);
            PlayerManager playerManager = PlayerManager.getInstance();

            WrappedPlayerDatabase db = playerManager.getDatabaseRedis(punishedUuid);

            db.put(PlayerDatabaseKeys.MUTE_DURATION, punishment);

            playerManager.update(db);

            new SendPlayerMessageAction(punishedName, getPunishmentMessage(punishment))
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
     * Remueve de la base de datos del jugador
     * su estado como silenciado.
     * Este método NO registrará la sanción como expirada.
     * Para ello, {@link #registerFinishedMute(Punishment.ExpiredPunishment)}.
     *
     * @param punishedName nombre del jugador a quien quitar el silencio
     * @return indiferente
     */

    @Override
    public boolean removePunishment(String punishedName) {
        CUtils.warnSyncCall();

        UUID uuid = PlayerUtils.getUUIDByName(punishedName);

        Preconditions.checkNotNull(uuid);

        if (Core.getInstance().getServerManager().isPlayerOnline(punishedName)) {
            PlayerManager playerManager = PlayerManager.getInstance();
            WrappedPlayerDatabase db = playerManager.getDatabaseRedis(uuid);

            db.remove(PlayerDatabaseKeys.MUTE_DURATION);

            playerManager.update(db);

            new SendPlayerMessageAction(punishedName, Lang.SUCCESS_COLOR + "You are no longer muted.")
                    .push(JedisManager.getInstance().getPool());

            return true;
        }

        MySQLManager.update("DELETE FROM dustbans.mutes WHERE uuid = ?;", uuid.toString());

        return true;
    }

    /**
     * Registra un silencio como expirado.
     *
     * @param mute clase que contiene los datos de la sanción expirada
     */

    @Async
    public void registerFinishedMute(Punishment.ExpiredPunishment mute) {
        CUtils.warnSyncCall();

        int id = mute.getPunishmentId();
        UUID uuid = mute.getPunishedUuid();
        String reason = mute.getReason();
        String punisherName = mute.getPunisherName();
        Timestamp date = mute.getDate();
        Timestamp expiration = mute.getExpiration();
        String unmutedBy = mute.getUnpunishedBy();
        Timestamp unmutedAt = mute.getUnpunishedAt();

        Preconditions.checkNotNull(expiration, "A mute punishment cannot be non-expirable.");

        MySQLManager.update("INSERT INTO dustbans.expired_mutes " +
                        "(mute_id, uuid, reason, muted_by, mute_date, expiration, unmuted_by, unmuted_at)" +
                        " VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
                id, uuid.toString(), reason, punisherName, date, expiration, unmutedBy, unmutedAt);
    }

    @Override
    public boolean hasActivePunishment(UUID punishedUuid) {
        return currentPunishment(punishedUuid).isPresent();
    }

    @Override
    public Optional<Punishment> currentPunishment(UUID punishedUuid) {
        CUtils.warnSyncCall();

        Preconditions.checkNotNull(punishedUuid);

        CachedRowSet set = MySQLManager.query("SELECT * FROM dustbans.mutes WHERE uuid = ? AND expiration > NOW();", punishedUuid.toString());

        try {
            if (set.next()) {
                int id = set.getInt("id");
                Timestamp date = set.getTimestamp("date");
                Timestamp expiration = set.getTimestamp("expiration");
                String reason = set.getString("reason");
                String mutedBy = set.getString("muted_by");

                Punishment punishment = new Punishment(id, PunishmentType.MUTE, punishedUuid, mutedBy, reason, date, expiration, null);

                return Optional.of(punishment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public String getPunishmentMessage(Punishment punishment) {
        String reason = punishment.getReason();
        Timestamp expiration = punishment.getExpiration();
        int id = punishment.getPunishmentId();

        String expires = DATE_FORMAT.format(expiration);

        return String.format(MESSAGE, reason, expires, id);
    }

    /**
     * Obtiene una lista ordenada (últimos silencios primeros)
     * de los silencios que están MARCADOS como expirados en la tabla 'dustbans.expired_mutes'.
     * Este método NO obtendrá los silencios que están listas para que expiren.
     *
     * @param punishedName nombre del jugador
     * @return lista de silencios que el jugador ha tenido
     */

    @Override
    public List<Punishment.ExpiredPunishment> fetchPunishments(String punishedName) {
        CUtils.warnSyncCall();

        UUID uuid = PlayerUtils.getUUIDByName(punishedName);

        Preconditions.checkNotNull(uuid);

        List<Punishment.ExpiredPunishment> expiredMutes = new ArrayList<>();
        CachedRowSet set = MySQLManager.query("SELECT * FROM dustbans.expired_mutes WHERE uuid = ? ORDER BY mute_date DESC;", uuid.toString());

        try {
            while (set.next()) {
                int id = set.getInt("id");
                String punisherName = set.getString("muted_by");
                String reason = set.getString("reason");
                Timestamp date = set.getTimestamp("mute_date");
                Timestamp expiration = set.getTimestamp("expiration");
                String unmutedBy = set.getString("unmuted_by");
                Timestamp unmutedAt = set.getTimestamp("unmuted_at");

                Punishment.ExpiredPunishment expiredMute = new Punishment.ExpiredPunishment(
                        id, PunishmentType.MUTE, uuid, punisherName,
                        reason, date, expiration, null,
                        unmutedBy, unmutedAt
                );

                expiredMutes.add(expiredMute);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return expiredMutes;
    }

}
