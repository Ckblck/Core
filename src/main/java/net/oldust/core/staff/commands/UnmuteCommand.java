package net.oldust.core.staff.commands;

import net.oldust.core.actions.types.DispatchMessageAction;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.staff.punish.Punishment;
import net.oldust.core.staff.punish.PunishmentType;
import net.oldust.core.staff.punish.types.MutePunishment;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import net.oldust.sync.JedisManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UnmuteCommand extends InheritedCommand<StaffPlugin> {
    private static final MutePunishment HANDLER = (MutePunishment) PunishmentType.MUTE.getHandler();
    private static final String STAFF_ALERT_MESSAGE = CUtils.color("#ff443b[!] #80918a #fcba03 %s#80918a has unmuted #fcba03%s#80918a.");

    public UnmuteCommand(StaffPlugin plugin) {
        super(plugin, "unmute", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"), LangSound.ERROR);

                return;
            }

            String name = args[0];
            CompletableFuture<UUID> future = CompletableFuture.supplyAsync(() ->
                    PlayerUtils.getUUIDByName(name));

            future.thenAcceptAsync(uuid -> {
                if (uuid == null) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "That player does not exist in the database.", LangSound.ERROR);

                    return;
                }

                Optional<Punishment> punishment = HANDLER.currentPunishment(uuid);

                if (punishment.isEmpty()) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "The specified player is not muted.", LangSound.ERROR);

                    return;
                }

                HANDLER.removePunishment(name);

                Punishment.ExpiredPunishment mute = new Punishment.ExpiredPunishment(
                        punishment.get(), sender.getName(), new Timestamp(System.currentTimeMillis())
                ); // TODO: Change name of console (sender.getName()).

                HANDLER.registerFinishedMute(mute);

                String staffMessage = String.format(STAFF_ALERT_MESSAGE, sender.getName(), name);

                new DispatchMessageAction(DispatchMessageAction.Channel.NETWORK_WIDE, db -> {
                    PlayerRank rank = db.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);

                    return rank.isStaff();
                }, false, staffMessage, Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 0.5F, 1F)
                        .push(JedisManager.getInstance().getPool());

                if (!(sender instanceof Player)) {
                    CUtils.msg(sender, Lang.SUCCESS_COLOR + "The player " + name + " is no longer muted.");
                }

            });
        };
    }

}
