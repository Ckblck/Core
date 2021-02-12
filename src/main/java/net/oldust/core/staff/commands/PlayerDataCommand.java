package net.oldust.core.staff.commands;

import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.staff.playerdata.PlayerDataInventory;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDataCommand extends InheritedCommand<StaffPlugin> {

    public PlayerDataCommand(StaffPlugin plugin) {
        super(plugin, "pdata", List.of("playerdata", "pld", "pldata"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            Player player = (Player) sender;

            if (args.length == 0) {
                CUtils.msg(player, String.format(Lang.MISSING_ARGUMENT_FORMATTABLE, "nickname", LangSound.ERROR));

                return;
            }

            String name = args[0];
            CompletableFuture<UUID> future = CompletableFuture.supplyAsync(() ->
                    PlayerUtils.getUUIDByName(name));

            future.thenAcceptAsync(uuid -> {
                if (uuid == null) {
                    CUtils.msg(player, Lang.ERROR_COLOR + "That player does not exist in the database.", LangSound.ERROR);

                    return;
                }

                String ipAddress = PlayerUtils.getIpAddress(uuid);

                new PlayerDataInventory(player, name, uuid, ipAddress);
            });

        };
    }

}
