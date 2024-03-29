package net.oldust.core.staff.commands;

import net.oldust.core.Core;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import net.oldust.sync.ServerManager;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.Optional;

public class FindCommand extends InheritedCommand<StaffPlugin> {

    public FindCommand(StaffPlugin plugin) {
        super(plugin, "find", Collections.singletonList("fnd"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            CUtils.runAsync(() -> {
                if (args.length == 0) {
                    CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATTABLE, "nickname", LangSound.ERROR));

                    return;
                }

                ServerManager serverManager = Core.getInstance().getServerManager();
                String player = args[0];

                Optional<String> server = serverManager.getPlayerServer(player);

                server.ifPresentOrElse(sv ->
                                CUtils.msg(sender, Lang.SUCCESS_COLOR + player + " is connected at " + sv + "."),
                        () -> CUtils.msg(sender, Lang.PLAYER_OFFLINE, LangSound.ERROR));

            });

        };
    }

}
