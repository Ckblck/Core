package com.oldust.core.staff.mode.command;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.mode.StaffMode;
import com.oldust.core.utils.interactive.InteractivePanel;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public class ModeCommand extends InheritedCommand<StaffPlugin> {

    public ModeCommand(StaffPlugin plugin) {
        super(plugin, "v", null);
    }

    @Override
    public BiConsumer<CommandSender, String[]> onCommand() {
        return (sender, args) -> {
            if (isNotPlayer(sender)) return;

            Player player = (Player) sender;
            WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());

            if (database.contains(PlayerDatabaseKeys.STAFF_MODE)) {
                InteractivePanel panel = database.getValue(PlayerDatabaseKeys.STAFF_MODE).asClass(InteractivePanel.class);
                panel.exit(player);

                database.remove(PlayerDatabaseKeys.STAFF_MODE);
                PlayerManager.getInstance().update(database);
            } else {
                new StaffMode(player, database);
            }

        };
    }

}
