package net.oldust.core.commons.commands;

import net.oldust.core.Core;
import net.oldust.core.actions.types.SendPlayerMessageAction;
import net.oldust.core.commons.CommonsPlugin;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.punish.Punishment;
import net.oldust.core.staff.punish.PunishmentType;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.PlayerUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Async;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.JedisManager;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.ServerManager;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.Savable;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
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

            CUtils.runAsync(() ->
                    sendMsg(player, target, message));
        };
    }

    @Async
    public void sendMsg(Player player, String target, String message) {
        CUtils.warnSyncCall();

        PlayerManager playerManager = PlayerManager.getInstance();
        WrappedPlayerDatabase playerDatabase = playerManager.getDatabase(player);
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

        PlayerRank playerRank = PlayerRank.getPlayerRank(player);
        boolean locallyConnected = PlayerUtils.isLocallyConnected(target);

        if (locallyConnected) {
            Player targetPlayer = Bukkit.getPlayer(target);
            assert targetPlayer != null;

            WrappedPlayerDatabase targetDatabase = playerManager.getDatabase(targetPlayer);

            if (targetDatabase.contains(PlayerDatabaseKeys.NO_MPS)) {
                CUtils.msg(player, Lang.ERROR_COLOR + "That player cannot receive private messages.");

                return;
            }

            PlayerRank targetRank = targetDatabase.getValue(PlayerDatabaseKeys.RANK).asClass(PlayerRank.class);
            String finalMessage = buildFormat(player, targetPlayer.getName(), playerRank, targetRank);

            player.sendMessage(finalMessage + message);
            targetPlayer.sendMessage(finalMessage + message);

            // Establecemos el último jugador que contactó para el comando /reply

            playerDatabase.put(PlayerDatabaseKeys.LAST_PLAYER_MESSAGED, target);
            targetDatabase.put(PlayerDatabaseKeys.LAST_PLAYER_MESSAGED, player.getName());

            playerManager.update(playerDatabase);
            playerManager.update(targetDatabase);

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
            WrappedPlayerDatabase targetDatabase = playerManager.getDatabaseRedis(targetUuid);

            if (targetDatabase.contains(PlayerDatabaseKeys.NO_MPS)) {
                CUtils.msg(player, Lang.ERROR_COLOR + "That player cannot receive private messages.");

                return;
            }

            PlayerRank targetRank = targetDatabase
                    .getValue(PlayerDatabaseKeys.RANK)
                    .asClass(PlayerRank.class);

            String finalMessage = buildFormat(player, capitalizedName, playerRank, targetRank) + message;

            player.sendMessage(finalMessage);

            // Establecemos el último jugador que contactó para el comando /reply

            playerDatabase.put(PlayerDatabaseKeys.LAST_PLAYER_MESSAGED, target);
            targetDatabase.put(PlayerDatabaseKeys.LAST_PLAYER_MESSAGED, player.getName());

            playerManager.update(playerDatabase);
            playerManager.update(targetDatabase);

            // Enviamos acción cross-server

            new SendPlayerMessageAction(target, finalMessage)
                    .push(JedisManager.getInstance().getPool());

        }, () -> CUtils.msg(player, Lang.PLAYER_OFFLINE));
    }

    private String buildFormat(Player player, String targetPlayer, PlayerRank playerRank, PlayerRank targetRank) {
        return CUtils.color(playerRank.getPrefix()
                + player.getName() + " "
                + Lang.ARROW + " "
                + targetRank.getPrefix() + targetPlayer
                + " #fcba03 » &r");
    }

}
