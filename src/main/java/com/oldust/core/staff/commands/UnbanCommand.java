package com.oldust.core.staff.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.punish.Punishment;
import com.oldust.core.staff.punish.PunishmentType;
import com.oldust.core.staff.punish.types.BanPunishment;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.lambda.TriConsumer;
import org.bukkit.command.CommandSender;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public class UnbanCommand extends InheritedCommand<StaffPlugin> {

    public UnbanCommand(StaffPlugin plugin) {
        super(plugin, "unban", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

                return;
            }

            String senderName = sender.getName(); // TODO: Custom name for Console
            String punished = args[0];
            BanPunishment handler = (BanPunishment) PunishmentType.BAN.getHandler();

            CUtils.runAsync(() -> {
                UUID uuid = PlayerUtils.getUUIDByName(punished);
                Optional<Punishment> punishment = handler.currentPunishment(uuid);

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
                BanPunishment.ExpiredBan expiredBan = new BanPunishment.ExpiredBan(currentPunishment, senderName, currentDate);

                handler.registerFinishedBan(expiredBan);

                CUtils.msg(sender, Lang.SUCCESS_COLOR + "The player " + punished + " has been unbanned.");
            });

        };
    }

}
