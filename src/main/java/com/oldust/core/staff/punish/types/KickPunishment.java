package com.oldust.core.staff.punish.types;

import com.oldust.core.actions.types.KickPlayerAction;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.staff.punish.Punishment;
import com.oldust.core.staff.punish.PunishmentType;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.sync.JedisManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.UUID;

public class KickPunishment implements Punishable {
    private static final PunishmentType TYPE = PunishmentType.KICK;
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

        Punishment punishment = new Punishment(id, TYPE, uuid, punisherName, reason, null, null, null);
        new KickPlayerAction(punishedName, getPunishmentMessage(punishment))
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

}
