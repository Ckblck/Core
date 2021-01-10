package net.oldust.core.staff.commands;

import com.google.common.net.InetAddresses;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.staff.logs.LogsInventory;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

@SuppressWarnings("UnstableApiUsage")
public class LogCommand extends InheritedCommand<StaffPlugin> {

    public LogCommand(StaffPlugin plugin) {
        super(plugin, "log", Collections.singletonList("logs"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname", LangSound.ERROR));

                return;
            }

            CUtils.runAsync(() -> {
                String target = args[0];
                boolean isIp = InetAddresses.isInetAddress(target);

                String nickname = (isIp)
                        ? PlayerUtils.getPlayerNameByIp(target)
                        : (PlayerUtils.nicknameExistsDB(target) ? target : null);

                if (nickname == null && !isIp) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That player does not exist in the database.", LangSound.ERROR);
                } else if (nickname == null) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That IP Address could not be found in the database.", LangSound.ERROR);
                }

                new LogsInventory(((Player) sender), nickname);

                CUtils.msg(sender, Lang.SUCCESS_COLOR + "Processing...");
            });

        };
    }

}
