package com.oldust.core.staff.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
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
            WrappedPlayerDatabase database = playerManager.getDatabase(player);
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
