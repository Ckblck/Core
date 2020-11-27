package com.oldust.core.staff.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class StaffChatCommand extends InheritedCommand<StaffPlugin> {

    public StaffChatCommand(StaffPlugin plugin) {
        super(plugin, "a", Arrays.asList("sc", "staffchat"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender) || isNotStaff(sender)) return;

            Player player = (Player) sender;
            PlayerManager manager = PlayerManager.getInstance();

            WrappedPlayerDatabase db = manager.getDatabase(player.getUniqueId());
            boolean staffChat = db.contains(PlayerDatabaseKeys.STAFF_CHAT);

            if (staffChat) {
                db.remove(PlayerDatabaseKeys.STAFF_CHAT);

                CUtils.msg(sender, Lang.SUCCESS_COLOR + "Exited from the Staff chat.");
            } else {
                db.put(PlayerDatabaseKeys.STAFF_CHAT, true);

                CUtils.msg(sender, Lang.SUCCESS_COLOR + "Entered Staff chat.");
            }

            manager.update(db);
        };
    }

}
