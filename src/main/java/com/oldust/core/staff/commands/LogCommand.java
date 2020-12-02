package com.oldust.core.staff.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.staff.logs.LogsInventory;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.lambda.TriConsumer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.UUID;

public class LogCommand extends InheritedCommand<StaffPlugin> {

    public LogCommand(StaffPlugin plugin) {
        super(plugin, "log", Collections.singletonList("logs"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;
            if (isNotPlayer(sender)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

                return;
            }

            CUtils.runAsync(() -> {
                UUID uuid = PlayerUtils.getUUIDByName(args[0]);

                if (uuid == null) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That player does not exist in the database.");

                    return;
                }

                new LogsInventory(((Player) sender), uuid);
            });

            CUtils.msg(sender, Lang.SUCCESS_COLOR + "Processing...");
        };
    }

}
