package com.oldust.core.staff.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.punish.PunishmentType;
import com.oldust.core.staff.punish.types.MutePunishment;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.core.utils.lang.Lang;
import org.bukkit.command.CommandSender;

import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MuteCommand extends InheritedCommand<StaffPlugin> {
    private static final MutePunishment HANDLER = (MutePunishment) PunishmentType.MUTE.getHandler();

    public MuteCommand(StaffPlugin plugin) {
        super(plugin, "mute", null);
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

                if (args.length == 1) {
                    CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "duration"));

                    return;
                }

                TemporalAmount duration;

                try {
                    duration = CUtils.parseLiteralTime(args[1]);
                } catch (DateTimeParseException e) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "The provided duration is not correct.");

                    return;
                }

                if (args.length == 2) {
                    CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "reason"));

                    return;
                }

                String name = args[0];
                String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                if (reason.length() > 34) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That reason is too long.");

                    return;
                }

                UUID uuid = PlayerUtils.getUUIDByName(name);

                if (uuid == null) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That player does not exist in the database.");

                    return;
                }

                if (HANDLER.hasActivePunishment(uuid)) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "The specified player is already muted!");

                    return;
                }

                HANDLER.punish(sender.getName(), name, uuid, duration, reason, false); // TODO: Custom name for console?
            });

        };
    }

}
