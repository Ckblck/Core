package com.oldust.core.staff.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.playerdata.PlayerDataInventory;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.lambda.TriConsumer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class PlayerDataCommand extends InheritedCommand<StaffPlugin> {

    public PlayerDataCommand(StaffPlugin plugin) {
        super(plugin, "pdata", List.of("playerdata", "pld", "pldata"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            Player player = (Player) sender;

            if (args.length == 0) {
                CUtils.msg(player, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

                return;
            }

            CUtils.runAsync(() -> {
                String name = args[0];
                UUID uuid = PlayerUtils.getUUIDByName(name);

                if (uuid == null) {
                    CUtils.msg(player, Lang.ERROR_COLOR + "That player does not exist in the database.");

                    return;
                }

                new PlayerDataInventory(player, name, uuid);
            });

        };
    }

}
