package net.oldust.core.staff.commands;

import net.oldust.core.actions.types.DispatchMessageAction;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.staff.punish.Punishment;
import net.oldust.core.staff.punish.PunishmentType;
import net.oldust.core.staff.punish.types.BanPunishment;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.JedisManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UnbanCommand extends InheritedCommand<StaffPlugin> {
    private static final String STAFF_ALERT_MESSAGE = CUtils.color("#ff443b[!] #80918a #fcba03 %s#80918a has unbanned #fcba03%s#80918a.");

    public UnbanCommand(StaffPlugin plugin) {
        super(plugin, "unban", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            CompletableFuture<Boolean> future = isNotAboveOrEqual(sender, PlayerRank.MOD);

            future.thenAcceptAsync(notAbove -> {
                if (notAbove) return;

                if (args.length == 0) {
                    CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

                    return;
                }

                String senderName = sender.getName(); // TODO: Custom name for Console
                String punished = args[0];
                BanPunishment handler = (BanPunishment) PunishmentType.BAN.getHandler();

                UUID uuid = PlayerUtils.getUUIDByName(punished);
                Optional<Punishment> punishment;

                if (uuid == null) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That player does not exist in the database.");

                    return;
                }

                punishment = handler.currentPunishment(uuid);

                if (punishment.isEmpty()) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "The specified player is not banned!");

                    return;
                }

                Punishment currentPunishment = punishment.get();
                boolean success = handler.removePunishment(punished);

                if (!success) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "Could not remove the current punishment of the player. Seems like it disappeared.");

                    return;
                }

                Timestamp currentDate = new Timestamp(System.currentTimeMillis());
                Punishment.ExpiredPunishment expiredBan = new Punishment.ExpiredPunishment(currentPunishment, senderName, currentDate);

                handler.registerFinishedBan(expiredBan);

                String staffMessage = String.format(STAFF_ALERT_MESSAGE, senderName, punished);

                new DispatchMessageAction(DispatchMessageAction.Channel.SERVER_WIDE, db -> {
                    return db.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class).isStaff();
                }, false, staffMessage, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F, 1F)
                        .push(JedisManager.getInstance().getPool());

                if (!(sender instanceof Player)) {
                    CUtils.msg(sender, Lang.SUCCESS_COLOR + "The player " + punished + " is no longer banned.");
                }
            });

        };
    }

}
