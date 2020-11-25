package com.oldust.core.staff.mode.command;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.mode.StaffModeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.function.BiConsumer;

public class VanishCommand extends InheritedCommand<StaffPlugin> {

    public VanishCommand(StaffPlugin plugin) {
        super(plugin, "vn", Collections.singletonList("vanish"));
    }

    @Override
    public BiConsumer<CommandSender, String[]> onCommand() {
        return (sender, args) -> {
            if (isNotPlayer(sender)) return;

            StaffPlugin plugin = getPlugin();
            StaffModeManager staffModeManager = plugin.getStaffModeManager();

            staffModeManager.switchState((Player) sender);
        };
    }

}
