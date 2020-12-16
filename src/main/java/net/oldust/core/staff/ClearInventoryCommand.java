package net.oldust.core.staff;

import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class ClearInventoryCommand extends InheritedCommand<StaffPlugin> {

    public ClearInventoryCommand(StaffPlugin plugin) {
        super(plugin, "clearinventory", Collections.singletonList("ci"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            Player player = (Player) sender;
            player.getInventory().clear();

            CUtils.msg(sender, Lang.SUCCESS_COLOR_ALT + "Your inventory has been cleared.");
        };
    }

}
