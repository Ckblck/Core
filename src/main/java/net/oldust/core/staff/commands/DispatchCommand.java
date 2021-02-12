package net.oldust.core.staff.commands;

import net.oldust.core.Core;
import net.oldust.core.actions.types.DispatchCommandAction;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import net.oldust.sync.jedis.JedisManager;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

public class DispatchCommand extends InheritedCommand<StaffPlugin> {

    public DispatchCommand(StaffPlugin plugin) {
        super(plugin, "c", Arrays.asList("cmd", "command"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotAboveOrEqual(sender, PlayerRank.ADMIN)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATTABLE, "'*' or <server name>", LangSound.ERROR));

                return;
            }

            if (args.length == 1) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATTABLE, "command", LangSound.ERROR));

                return;
            }

            CUtils.runAsync(() -> {
                String serverName = args[0];
                String[] command = Arrays.copyOfRange(args, 1, args.length);
                boolean validServer = serverName.equals("*") || Core.getInstance().getServerManager().contains(serverName);

                if (!validServer) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "The specified server is either invalid or not connected.", LangSound.ERROR);

                    return;
                }

                new DispatchCommandAction(sender.getName(), serverName, command)
                        .push(JedisManager.getInstance().getPool());

                CUtils.msg(sender, Lang.SUCCESS_COLOR + "Command correctly propagated.");
            });

        };
    }

}
