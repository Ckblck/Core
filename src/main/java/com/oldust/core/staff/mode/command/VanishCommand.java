package com.oldust.core.staff.mode.command;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.mode.StaffModeManager;
import com.oldust.core.utils.lambda.TriConsumer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class VanishCommand extends InheritedCommand<StaffPlugin> {

    public VanishCommand(StaffPlugin plugin) {
        super(plugin, "vn", Collections.singletonList("vanish"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            StaffPlugin plugin = getPlugin();
            StaffModeManager staffModeManager = plugin.getStaffModeManager();

            staffModeManager.switchState((Player) sender);
        };
    }

}
