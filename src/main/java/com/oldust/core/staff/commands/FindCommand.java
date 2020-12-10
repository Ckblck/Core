package com.oldust.core.staff.commands;

import com.oldust.core.Core;
import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.sync.ServerManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FindCommand extends InheritedCommand<StaffPlugin> {

    public FindCommand(StaffPlugin plugin) {
        super(plugin, "find", Collections.singletonList("fnd"));
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

                ServerManager serverManager = Core.getInstance().getServerManager();
                String player = args[0];

                Optional<String> server = serverManager.getPlayerServer(player);

                server.ifPresentOrElse(sv -> {
                    CUtils.msg(sender, Lang.SUCCESS_COLOR + player + " is connected at " + sv + ".");
                }, () -> CUtils.msg(sender, Lang.PLAYER_OFFLINE));

            });

        };
    }

}
