package com.oldust.core.staff;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.core.utils.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class ClearInventoryCommand extends InheritedCommand<StaffPlugin> {

    public ClearInventoryCommand(StaffPlugin plugin) {
        super(plugin, "clearinventory", Collections.singletonList("ci"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            CompletableFuture<Boolean> future = isNotAboveOrEqual(sender, PlayerRank.MOD);
            Player player = (Player) sender;

            future.thenAccept(notAbove -> {
                if (notAbove) return;

                CUtils.runSync(() -> player.getInventory().clear());

                CUtils.msg(sender, Lang.SUCCESS_COLOR_ALT + "Your inventory has been cleared.");
            });

        };
    }

}
