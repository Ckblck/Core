package net.oldust.core.staff.commands;

import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.staff.punish.PunishmentType;
import net.oldust.core.staff.punish.types.KickPunishment;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class KickCommand extends InheritedCommand<StaffPlugin> {
    private static final KickPunishment HANDLER = (KickPunishment) PunishmentType.KICK.getHandler();

    public KickCommand(StaffPlugin plugin) {
        super(plugin, "kick", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname", LangSound.ERROR));

                return;
            }

            if (args.length == 1) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "reason", LangSound.ERROR));

                return;
            }

            String senderName = sender.getName();
            String player = args[0];
            String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            if (reason.length() > 34) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "That reason is too long.", LangSound.ERROR);

                return;
            }

            CUtils.runAsync(() -> {
                boolean success = HANDLER.punish(senderName, player, null, reason, false);

                if (!success) {
                    CUtils.msg(sender, Lang.PLAYER_OFFLINE, LangSound.ERROR);
                }
            });

        };
    }

}
