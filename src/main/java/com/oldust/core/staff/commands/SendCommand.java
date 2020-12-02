package com.oldust.core.staff.commands;

import com.oldust.core.Core;
import com.oldust.core.actions.types.SendToServerAction;
import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.sync.JedisManager;
import com.oldust.sync.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

public class SendCommand extends InheritedCommand<StaffPlugin> {

    public SendCommand(StaffPlugin plugin) {
        super(plugin, "send", Collections.singletonList("sendall"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (label.equalsIgnoreCase("send")) {
                if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

                send(sender, args);
            } else {
                if (isNotAboveOrEqual(sender, PlayerRank.ADMIN)) return;

                sendAll(sender, args);
            }

        };
    }

    private void send(CommandSender sender, String[] args) {
        if (args.length < 2) {
            CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, (args.length == 0) ? "nickname" : "server_name"));

            return;
        }

        ServerManager serverManager = Core.getInstance().getServerManager();
        String player = args[0];
        String server = args[1];

        CUtils.runAsync(() -> {
            boolean serverExists = serverManager.contains(server);

            if (!serverExists) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "That server does not exist.");

                return;
            }

            boolean playerConnected = serverManager.isPlayerOnline(player);

            if (!playerConnected) {
                CUtils.msg(sender, Lang.PLAYER_OFFLINE);

                return;
            }

            new SendToServerAction(player, server).push(JedisManager.getInstance().getPool());
            CUtils.msg(sender, Lang.SUCCESS_COLOR_ALT + "Successfully sent player " + player + " to server " + server + ".");
        });

    }

    private void sendAll(CommandSender sender, String[] args) {
        if (args.length == 0) {
            CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "server"));

            return;
        }

        if (Bukkit.getOnlinePlayers().isEmpty()) {
            CUtils.msg(sender, Lang.ERROR_COLOR + "This server is empty.");

            return;
        }

        String server = args[0];

        CUtils.runAsync(() -> {
            boolean serverExists = Core.getInstance().getServerManager().contains(server);

            if (!serverExists) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "That server does not exist.");

                return;
            }

            Collection<? extends Player> players = Bukkit.getOnlinePlayers();

            for (Player player : players) {
                PlayerUtils.sendToServer(player, server);
            }

            CUtils.msg(sender, Lang.SUCCESS_COLOR_ALT + "Successfully sent " + players.size() + " players to server " + server + ".");
        });

    }

}
