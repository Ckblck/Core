package net.oldust.core.commons.commands;

import net.oldust.core.commons.CommonsPlugin;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class PingCommand extends InheritedCommand<CommonsPlugin> {

    public PingCommand(CommonsPlugin plugin) {
        super(plugin, "ping", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            if (args.length == 0) {
                showPing(sender, ((Player) sender));
            } else {
                String who = args[0];
                boolean connected = PlayerUtils.isLocallyConnected(who);

                if (!connected) {
                    CUtils.msg(sender, Lang.PLAYER_OFFLINE);

                    return;
                }

                showPing(sender, Objects.requireNonNull(Bukkit.getPlayer(who)));
            }
        };
    }

    private void showPing(CommandSender requester, Player player) {
        CUtils.msg(requester, Lang.SUCCESS_COLOR_ALT + player.getName() + "'s ping is: &r" + player.spigot().getPing() + "ms" + Lang.SUCCESS_COLOR_ALT + ".");
    }

}
