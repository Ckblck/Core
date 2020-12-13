package net.oldust.core.staff.commands;

import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class StaffChatCommand extends InheritedCommand<StaffPlugin> {

    public StaffChatCommand(StaffPlugin plugin) {
        super(plugin, "a", Arrays.asList("sc", "staffchat"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            CompletableFuture<Boolean> future = isNotStaff(sender);

            future.thenAcceptAsync(notStaff -> {
                if (notStaff) {
                    CUtils.msg(sender, Lang.NO_PERMISSIONS);

                    return;
                }

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
            });

        };
    }

}
