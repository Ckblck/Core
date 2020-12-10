package com.oldust.core.staff.commands;

import com.oldust.core.actions.types.DispatchMessageAction;
import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.punish.Punishment;
import com.oldust.core.staff.punish.PunishmentType;
import com.oldust.core.staff.punish.types.MutePunishment;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.sync.JedisManager;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public class UnmuteCommand extends InheritedCommand<StaffPlugin> {
    private static final MutePunishment HANDLER = (MutePunishment) PunishmentType.MUTE.getHandler();
    private static final String STAFF_ALERT_MESSAGE = CUtils.color("#ff443b[!] #80918a #fcba03 %s#80918a has unmuted #fcba03%s#80918a.");

    public UnmuteCommand(StaffPlugin plugin) {
        super(plugin, "unmute", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

                return;
            }

            CUtils.runAsync(() -> {
                String name = args[0];
                UUID uuid = PlayerUtils.getUUIDByName(name);

                if (uuid == null) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That player does not exist in the database.");

                    return;
                }

                Optional<Punishment> punishment = HANDLER.currentPunishment(uuid);

                if (punishment.isEmpty()) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "The specified player is not muted.");

                    return;
                }

                HANDLER.removePunishment(name);

                Punishment.ExpiredPunishment mute = new Punishment.ExpiredPunishment(
                        punishment.get(), sender.getName(), new Timestamp(System.currentTimeMillis())
                ); // TODO: Change name of console (sender.getName()).

                HANDLER.registerFinishedMute(mute);

                String staffMessage = String.format(STAFF_ALERT_MESSAGE, sender.getName(), name);

                new DispatchMessageAction(DispatchMessageAction.Channel.SERVER_WIDE, PlayerRank::isStaff, false, staffMessage, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F, 1F)
                        .push(JedisManager.getInstance().getPool());

                if (!(sender instanceof Player)) {
                    CUtils.msg(sender, Lang.SUCCESS_COLOR + "The player " + name + " is no longer muted.");
                }

            });

        };
    }

}
