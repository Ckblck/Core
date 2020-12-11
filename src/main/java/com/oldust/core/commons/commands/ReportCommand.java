package com.oldust.core.commons.commands;

import com.oldust.core.Core;
import com.oldust.core.commons.CommonsPlugin;
import com.oldust.core.commons.reports.Report;
import com.oldust.core.commons.reports.ReportsInventory;
import com.oldust.core.commons.reports.ReportsManager;
import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.core.utils.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class ReportCommand extends InheritedCommand<CommonsPlugin> {

    public ReportCommand(CommonsPlugin plugin) {
        super(plugin, "report", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            CompletableFuture<PlayerRank> future = CompletableFuture
                    .supplyAsync(() -> PlayerRank.getPlayerRank(sender));

            Player player = (Player) sender;
            ReportsManager reportsManager = getPlugin().getReportsManager();

            future.thenAccept(rank -> {
                if (rank.isEqualOrHigher(PlayerRank.MOD) && args.length == 0 /* TODO REMOVE THIS */) {
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

                    boolean hasReported = reportsManager.hasReported(player, reported);

                    if (hasReported) {
                        CUtils.msg(sender, Lang.ERROR_COLOR + "You already reported that player!");

                        return;
                    }

                    boolean offline = !Core.getInstance().getServerManager().isPlayerOnline(reported);

                    if (offline) {
                        CUtils.msg(sender, Lang.PLAYER_OFFLINE);

                        //return; TODO remove comment
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
