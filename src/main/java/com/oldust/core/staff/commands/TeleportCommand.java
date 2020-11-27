package com.oldust.core.staff.commands;

import com.oldust.core.Core;
import com.oldust.core.actions.types.SendToServerAction;
import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.sync.JedisManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class TeleportCommand extends InheritedCommand<StaffPlugin> {

    public TeleportCommand(StaffPlugin plugin) {
        super(plugin, "tp", Arrays.asList("teleport", "tphere"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (label.equalsIgnoreCase("tphere")
                    || args.length < 2
                    && isNotPlayer(sender)) {
                return;
            }

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

                return;
            }

            String who;
            String whom;

            if (args.length == 1) {
                who = args[0];
            } else {
                who = args[0];
                whom = args[1];
            }

            boolean tpHere = (label.equalsIgnoreCase("tphere"));

            if (tpHere) {
                Player player = Bukkit.getPlayer(who);

                if (player != null) {
                    player.teleport(((Player) sender).getLocation());
                } else {
                    new SendToServerAction(who, Core.getInstance().getServerManager().getCurrentServer()).push(JedisManager.getInstance().getPool());
                    // TODO
                }

            }

        };
    }

    private void teleport(Player from, Player to) {

    }

}
