package com.oldust.core.commons.commands;

import com.oldust.core.Core;
import com.oldust.core.actions.types.SendPlayerMessageAction;
import com.oldust.core.commons.CommonsPlugin;
import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.punish.Punishment;
import com.oldust.core.staff.punish.PunishmentType;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.PlayerUtils;
import com.oldust.core.utils.lambda.TriConsumer;
import com.oldust.sync.JedisManager;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.ServerManager;
import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.Savable;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Timestamp;
import java.util.*;

public class MsgCommand extends InheritedCommand<CommonsPlugin> {

    public MsgCommand(CommonsPlugin plugin) {
        super(plugin, "msg", List.of("m", "t", "whisper", "tell"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

                return;
            }

            if (args.length == 1) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "message"));

                return;
            }

            Player player = (Player) sender;
            String target = args[0];
            String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            if (player.getName().equalsIgnoreCase(target)) {
                CUtils.msg(player, Lang.ERROR_COLOR + "You can't send a message to yourself!");

                return;
            }

            PlayerManager playerManager = PlayerManager.getInstance();
            WrappedPlayerDatabase playerDatabase = playerManager.getDatabase(player.getUniqueId());
            Optional<Savable.WrappedValue> muted = playerDatabase.getValueOptional(PlayerDatabaseKeys.MUTE_DURATION);

            if (muted.isPresent()) {
                Punishment punishment = muted.get().asClass(Punishment.class);
                Timestamp expires = punishment.getExpiration();

                assert expires != null;

                if (expires.before(new Timestamp(System.currentTimeMillis()))) {
                    playerDatabase.remove(PlayerDatabaseKeys.MUTE_DURATION);
                    playerManager.update(playerDatabase);
                } else {
                    String muteMsg = PunishmentType.MUTE.getHandler().getPunishmentMessage(punishment);
                    CUtils.msg(player, muteMsg);

                    return;
                }

            }

            ServerManager serverManager = Core.getInstance().getServerManager();

            CUtils.runAsync(() -> {
                PlayerRank playerRank = PlayerRank.getPlayerRank(player);
                boolean locallyConnected = PlayerUtils.isLocallyConnected(target);

                if (locallyConnected) {
                    Player targetPlayer = Bukkit.getPlayer(target);

                    PlayerRank targetRank = PlayerRank.getPlayerRank(targetPlayer);

                    assert targetPlayer != null;

                    String finalMessage = buildFormat(player, targetPlayer.getName(), playerRank, targetRank);

                    player.sendMessage(finalMessage + message);
                    targetPlayer.sendMessage(finalMessage + message);

                    return;
                }

                Optional<String> targetServer = serverManager.getPlayerServer(target);

                targetServer.ifPresentOrElse(server -> {
                    Map<String, UUID> playersConnected = serverManager.getServer(server).getPlayersConnected();

                    String capitalizedName = playersConnected.keySet()
                            .stream()
                            .filter(playerName -> playerName.equalsIgnoreCase(target))
                            .findAny()
                            .orElseThrow(); // Capitalizamos su nombre de acuerdo como lo tiene.

                    UUID targetUuid = playersConnected.get(capitalizedName); // Obtenemos su UUID para obtener desde su database su rango.

                    PlayerRank targetRank = playerManager
                            .getDatabase(targetUuid)
                            .getValue(PlayerDatabaseKeys.RANK)
                            .asClass(PlayerRank.class);

                    String finalMessage = buildFormat(player, capitalizedName, playerRank, targetRank) + message;

                    player.sendMessage(finalMessage);

                    new SendPlayerMessageAction(target, finalMessage)
                            .push(JedisManager.getInstance().getPool());

                }, () -> CUtils.msg(sender, Lang.PLAYER_OFFLINE));

            });

        };
    }

    private String buildFormat(Player player, String targetPlayer, PlayerRank playerRank, PlayerRank targetRank) {
        return CUtils.color(playerRank.getPrefix()
                + player.getName() + " "
                + Lang.ARROW + " "
                + targetRank.getPrefix() + targetPlayer
                + " #fcba03 » &r");
    }

}
