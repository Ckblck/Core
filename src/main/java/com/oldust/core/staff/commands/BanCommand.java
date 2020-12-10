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
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BanCommand extends InheritedCommand<StaffPlugin> {

    public BanCommand(StaffPlugin plugin) {
        super(plugin, "ban", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            CompletableFuture<Boolean> future = isNotAboveOrEqual(sender, PlayerRank.MOD);

            future.thenAccept(notAbove -> {
                if (notAbove) return;

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
                Punishable<?> handler = PunishmentType.BAN.getHandler();
                boolean banIp = false;

                if (StringUtils.endsWithIgnoreCase(reason, "-ip")) {
                    reason = StringUtils.removeEnd(reason, "-ip");
                    banIp = true;
                }

                if (reason.length() > 34) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That reason is too long.");

                    return;
                }

                if (StringUtils.isWhitespace(reason)) {
                    CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "reason"));

                    return;
                }

                TemporalAmount finalDuration = duration;
                String finalReason = reason;
                boolean finalBanIp = banIp;

                UUID uuid = PlayerUtils.getUUIDByName(punishedName);

                if (uuid == null) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That player does not exist in the database.");

                    return;
                }

                boolean alreadyPunished = handler.hasActivePunishment(uuid);

                if (alreadyPunished) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That player is already banned!");

                    return;
                }

                handler.punish(senderName, punishedName, finalDuration, finalReason, finalBanIp);

            });

        };
    }

}
