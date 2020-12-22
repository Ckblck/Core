package net.oldust.core.commons.commands;

import net.oldust.core.Core;
import net.oldust.core.commons.CommonsPlugin;
import net.oldust.core.commons.reports.Report;
import net.oldust.core.commons.reports.ReportsInventory;
import net.oldust.core.commons.reports.ReportsManager;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;

public class ReportCommand extends InheritedCommand<CommonsPlugin> {

    public ReportCommand(CommonsPlugin plugin) {
        super(plugin, "report", Collections.singletonList("rp"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            PlayerRank rank = PlayerRank.getPlayerRank(sender);

            Player player = (Player) sender;
            ReportsManager reportsManager = getPlugin().getReportsManager();

            CUtils.runAsync(() -> {
                if (rank.isEqualOrHigher(PlayerRank.MOD)) {
                    new ReportsInventory(player, getPlugin());
                } else {
                    if (args.length == 0) {
                        CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

                        return;
                    }

                    if (args.length == 1) {
                        CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "reason"));

                        return;
                    }

                    String reported = args[0];
                    String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                    if (reported.equalsIgnoreCase(player.getName())) {
                        CUtils.msg(sender, Lang.ERROR_COLOR + "You can't report yourself!");

                        return;
                    }

                    boolean hasReported = reportsManager.hasReported(player, reported);

                    if (hasReported) {
                        CUtils.msg(sender, Lang.ERROR_COLOR + "You already reported that player!");

                        return;
                    }

                    boolean offline = !Core.getInstance().getServerManager().isPlayerOnline(reported);

                    if (offline) {
                        CUtils.msg(sender, Lang.PLAYER_OFFLINE);

                        return;
                    }

                    Report report = new Report(reported, player.getName(), reason, new Timestamp(System.currentTimeMillis()));

                    reportsManager.setReport(report);
                    reportsManager.registerReport(player, reported);

                    CUtils.msg(sender, Lang.SUCCESS_COLOR_ALT + "Your report has been submitted. A Staff member will soon review it. Thanks!");

                }
            });

        };
    }

}
