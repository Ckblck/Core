package net.oldust.core.staff.commands;

import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NoMpsCommand extends InheritedCommand<StaffPlugin> {

    public NoMpsCommand(StaffPlugin plugin) {
        super(plugin, "nomps", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;
            if (isNotAboveOrEqual(sender, PlayerRank.ADMIN)) return;

            Player player = (Player) sender;

            PlayerManager playerManager = PlayerManager.getInstance();
            WrappedPlayerDatabase database = playerManager.get(player);

            boolean mpsDisabled = !database.contains(PlayerDatabaseKeys.NO_MPS);

            if (mpsDisabled) {
                database.put(PlayerDatabaseKeys.NO_MPS, true);

                CUtils.msg(sender, Lang.SUCCESS_COLOR_ALT + "You are no longer receiving private messages.");
            } else {
                database.remove(PlayerDatabaseKeys.NO_MPS);

                CUtils.msg(sender, Lang.SUCCESS_COLOR_ALT + "You are receiving private messages again.");
            }

            playerManager.update(database);
        };
    }

}
