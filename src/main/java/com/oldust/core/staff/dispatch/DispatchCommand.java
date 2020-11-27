package com.oldust.core.staff.dispatch;

import com.oldust.core.Core;
import com.oldust.core.actions.types.DispatchCommandAction;
import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.sync.JedisManager;
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
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "'*' or <server name>"));

                return;
            }

            if (args.length == 1) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "command"));

                return;
            }

            String serverName = args[0];
            String[] command = Arrays.copyOfRange(args, 1, args.length);
            boolean validServer = serverName.equals("*") || Core.getInstance().getServerManager().contains(serverName);

            if (!validServer) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "El servidor proporcionado es inválido o no está encendido.");

                return;
            }

            new DispatchCommandAction(sender.getName(), serverName, command).push(JedisManager.getInstance().getPool());
            CUtils.msg(sender, Lang.SUCCESS_COLOR + "Comando propagado correctamente.");
        };
    }

}
