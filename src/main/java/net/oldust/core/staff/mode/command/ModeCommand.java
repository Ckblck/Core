package net.oldust.core.staff.mode.command;

import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.staff.mode.StaffModeManager;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
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

            CUtils.runAsync(() -> {
                Player player = (Player) sender;
                WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player);
                StaffModeManager modeManager = getPlugin().getStaffModeManager();

                if (database.contains(PlayerDatabaseKeys.STAFF_MODE)) {
                    modeManager.exitStaffMode(player, database);
                } else {
                    modeManager.setStaffMode(player, database);
                }
            });

        };
    }

}
