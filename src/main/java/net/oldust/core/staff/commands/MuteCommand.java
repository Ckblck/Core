package net.oldust.core.staff.commands;

import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.staff.punish.PunishmentType;
import net.oldust.core.staff.punish.types.MutePunishment;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
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
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATTABLE, "nickname", LangSound.ERROR));

                return;
            }

            if (args.length == 1) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATTABLE, "duration", LangSound.ERROR));

                return;
            }

            TemporalAmount duration;

            try {
                duration = CUtils.parseLiteralTime(args[1]);
            } catch (DateTimeParseException e) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "The provided duration is not correct.", LangSound.ERROR);

                return;
            }

            if (args.length == 2) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATTABLE, "reason", LangSound.ERROR));

                return;
            }

            String name = args[0];
            String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

            if (reason.length() > 34) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "That reason is too long.", LangSound.ERROR);

                return;
            }

            CompletableFuture<UUID> future = CompletableFuture.supplyAsync(() ->
                    PlayerUtils.getUUIDByName(name));

            future.thenAcceptAsync(uuid -> {
                if (uuid == null) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That player does not exist in the database.", LangSound.ERROR);

                    return;
                }

                if (HANDLER.hasActivePunishment(uuid)) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "The specified player is already muted!", LangSound.ERROR);

                    return;
                }

                HANDLER.punish(sender.getName(), name, uuid, duration, reason, false); // TODO: Custom name for console?
            });

        };
    }
}
