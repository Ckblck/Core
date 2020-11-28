package com.oldust.core.staff.mode.command;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.mode.StaffModeManager;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class ModeCommand extends InheritedCommand<StaffPlugin> {

    public ModeCommand(StaffPlugin plugin) {
        super(plugin, "v", Arrays.asList("staffmode", "sm", "stmode"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            Player player = (Player) sender;
            WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());
            StaffModeManager modeManager = getPlugin().getStaffModeManager();

            if (database.contains(PlayerDatabaseKeys.STAFF_MODE)) {
                modeManager.exitStaffMode(player, database);
            } else {
                modeManager.setStaffMode(player, database);
            }

        };
    }

}
