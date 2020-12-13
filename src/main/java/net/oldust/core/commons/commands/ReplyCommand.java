package net.oldust.core.commons.commands;

import net.oldust.core.commons.CommonsPlugin;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.Savable;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

public class ReplyCommand extends InheritedCommand<CommonsPlugin> {

    public ReplyCommand(CommonsPlugin plugin) {
        super(plugin, "reply", Collections.singletonList("r"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "message"));

                return;
            }

            CUtils.runAsync(() -> {
                Player player = (Player) sender;
                WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player);

                Optional<Savable.WrappedValue> optional = database.getValueOptional(PlayerDatabaseKeys.LAST_PLAYER_MESSAGED);

                optional.ifPresentOrElse(lastPlayer -> {
                    MsgCommand msgCommand = getPlugin().getMsgCommand();

                    String[] message = Arrays.copyOfRange(args, 1, args.length);
                    String msg = String.join(" ", message);

                    msgCommand.sendMsg(player, lastPlayer.asString(), msg);

                }, () -> CUtils.msg(player, Lang.ERROR_COLOR + "You don't have anyone to reply!"));

            });

        };
    }

}
