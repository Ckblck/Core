package net.oldust.core.staff.punish.types;

import com.google.common.base.Preconditions;
import net.oldust.core.Core;
import net.oldust.core.actions.types.DispatchMessageAction;
import net.oldust.core.actions.types.KickPlayerAction;
import net.oldust.core.mysql.MySQLManager;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.punish.Punishment;
import net.oldust.core.staff.punish.PunishmentType;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.JedisManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class KickPunishment implements Punishable<Punishment> {
    private static final String STAFF_ALERT_MESSAGE = CUtils.color("#ff443b[!] #80918a #fcba03 %s#80918a has kicked #fcba03%s#80918a due to: %s.");
    private static final String MESSAGE_STRUCTURE = CUtils.color(
            Lang.ERROR_COLOR +
                    "You have been kicked!" + "\n\n" +
                    "#a6a6a6 Reason: &f%s \n" +
                    "#a6a6a6 Kicked by: &f%s \n" +
                    "#a6a6a6 Kick ID: &f#%d \n\n"
    );

    /**
     * Expulsa el jugador del servidor.
     *
     * @param punisherName nombre de quien expulsa
     * @param punishedName nombre de quien es expulsado
     * @param duration     null, no utilizado
     * @param reason       razón de la expulsión
     * @param banIp        indiferente, solo aplica para {@link BanPunishment}
     * @return true si el jugador pudo ser expulsado ya que estaba conectado
     */

    @Override
    public boolean punish(String punisherName, String punishedName, @Nullable TemporalAmount duration, String reason, boolean banIp) {
        CUtils.warnSyncCall();

        Player player = Bukkit.getPlayer(punishedName);

        if (player == null) return false;

        UUID uuid = player.getUniqueId();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        CachedRowSet set = MySQLManager.updateWithGeneratedKeys("INSERT INTO dustbans.kicks (uuid, date, reason, kicked_by) VALUES (?, ?, ?, ?);",
                uuid.toString(), now, reason, punisherName);

        int id = -1;

        try {
            if (set.next()) {
                id = set.getInt(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        if (Core.getInstance().getServerManager().isPlayerOnline(punishedName)) {
            Punishment punishment = new Punishment(id, PunishmentType.KICK, uuid, punisherName, reason, null, null, null);

            new KickPlayerAction(punishedName, getPunishmentMessage(punishment))
                    .push(JedisManager.getInstance().getPool());
        }

        String staffMessage = String.format(STAFF_ALERT_MESSAGE, punisherName, punishedName, ChatColor.stripColor(reason));

        new DispatchMessageAction(DispatchMessageAction.Channel.SERVER_WIDE, db -> {
            PlayerRank rank = db.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

            return rank.isStaff();
        }, false, staffMessage, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F, 1F)
                .push(JedisManager.getInstance().getPool());

        return true;
    }

    @Override
    public boolean removePunishment(String punishedName) {
        throw new UnsupportedOperationException("Kick punishments cannot be removed.");
    }

    @Override
    public boolean hasActivePunishment(UUID punishedUuid) {
        throw new UnsupportedOperationException("Kick punishments are not durable. Therefore, they cannot be active.");
    }

    @Override
    public Optional<Punishment> currentPunishment(UUID punishedUuid) {
        throw new UnsupportedOperationException("Kick punishments are not durable. Therefore, they cannot be active.");
    }

    @Override
    public String getPunishmentMessage(Punishment punishment) {
        String reason = punishment.getReason();
        String punisher = punishment.getPunisherName();
        int punishmentId = punishment.getPunishmentId();

        return String.format(MESSAGE_STRUCTURE, reason, punisher, punishmentId);
    }

    @Override
    public List<Punishment> fetchPunishments(String punishedName) {
        CUtils.warnSyncCall();

        UUID uuid = PlayerUtils.getUUIDByName(punishedName);

        Preconditions.checkNotNull(uuid);

        List<Punishment> kicks = new ArrayList<>();
        CachedRowSet set = MySQLManager.query("SELECT id, kicked_by, reason, date FROM dustbans.kicks WHERE uuid = ? ORDER BY date DESC;", uuid.toString());

        try {
            while (set.next()) {
                int id = set.getInt("id");
                String punisherName = set.getString("kicked_by");
                String reason = set.getString("reason");
                Timestamp date = set.getTimestamp("date");

                Punishment kick = new Punishment(
                        id, PunishmentType.KICK, uuid, punisherName,
                        reason, date, null, null
                );

                kicks.add(kick);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return kicks;
    }

}
