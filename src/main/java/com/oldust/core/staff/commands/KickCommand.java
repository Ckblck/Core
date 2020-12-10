package com.oldust.core.staff.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.punish.PunishmentType;
import com.oldust.core.staff.punish.types.KickPunishment;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.lambda.TriConsumer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class KickCommand extends InheritedCommand<StaffPlugin> {
    private static final KickPunishment HANDLER = (KickPunishment) PunishmentType.KICK.getHandler();

    public KickCommand(StaffPlugin plugin) {
        super(plugin, "kick", null);
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
                    CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "reason"));

                    return;
                }

                String senderName = sender.getName();
                String player = args[0];
                String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                if (reason.length() > 34) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That reason is too long.");

                    return;
                }

                boolean success = HANDLER.punish(senderName, player, null, reason, false);

                if (!success) {
                    CUtils.msg(sender, Lang.PLAYER_OFFLINE);
                }

            });

        };
    }

}
