package com.oldust.core.staff.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.punish.PunishmentType;
import com.oldust.core.staff.punish.types.Punishable;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.lambda.TriConsumer;
import org.bukkit.command.CommandSender;

import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.UUID;

public class BanCommand extends InheritedCommand<StaffPlugin> {

    public BanCommand(StaffPlugin plugin) {
        super(plugin, "ban", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

                return;
            }

            if (args.length == 1) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "duration/reason"));

                return;
            }

            String senderName = sender.getName();
            String punishedName = args[0];
            TemporalAmount duration;

            try {
                duration = CUtils.parseLiteralTime(args[1]);
            } catch (DateTimeParseException ignored) {
                duration = null; // Permanente
            }

            String[] reasonRanged = (duration == null)
                    ? Arrays.copyOfRange(args, 1, args.length)
                    : Arrays.copyOfRange(args, 2, args.length);

            String reason = String.join(" ", reasonRanged);
            Punishable handler = PunishmentType.BAN.getHandler();
            TemporalAmount finalDuration = duration;

            CUtils.runAsync(() -> {
                UUID uuid = PlayerUtils.getUUIDByName(punishedName);
                boolean alreadyPunished = handler.hasActivePunishment(uuid);

                if (alreadyPunished) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That player is already banned!");

                    return;
                }

                handler.punish(senderName, punishedName, finalDuration, reason);
            });

        };
    }

}
