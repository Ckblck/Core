package net.oldust.core.staff.commands;

import net.oldust.core.Core;
import net.oldust.core.actions.types.SendToServerAction;
import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.JedisManager;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.ServerManager;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TeleportCommand extends InheritedCommand<StaffPlugin> {

    public TeleportCommand(StaffPlugin plugin) {
        super(plugin, "tp", Arrays.asList("teleport", "tphere"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            CompletableFuture<Boolean> future = isNotAboveOrEqual(sender, PlayerRank.MOD);

            future.thenAcceptAsync(notAbove -> {
                if (notAbove) return;

                if (args.length == 0) {
                    CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

                    return;
                }

                boolean tpHere = (label.equalsIgnoreCase("tphere"));
                boolean coordinates = !tpHere && args.length >= 3 && NumberUtils.isNumber(args[0]);

                if (tpHere) {
                    if (isNotPlayer(sender)) return;

                    parseTpHere(((Player) sender), args);
                } else if (coordinates) {
                    if (isNotPlayer(sender)) return;

                    tpCoordinates(sender, args);
                } else {
                    parseTp(sender, args);
                }
            });
        };
    }

    private void tpCoordinates(CommandSender sender, String[] args) {
        Player player = ((Player) sender);
        Location playerLocation = player.getLocation();

        boolean noXDifference = args[0].equals("~");
        boolean noYDifference = args[1].equals("~");
        boolean noZDifference = args[2].equals("~");

        boolean valid = (noXDifference || NumberUtils.isNumber(args[0]))
                && (noYDifference || NumberUtils.isNumber(args[1]))
                && (noZDifference || NumberUtils.isNumber(args[2]));

        if (!valid) {
            CUtils.msg(sender, Lang.ERROR_COLOR + "Wrong syntax! Example: /tp 0 40 50");

            return;
        }

        boolean hasPitch = args.length >= 4 && NumberUtils.isNumber(args[3]);
        boolean hasYaw = args.length >= 5 && NumberUtils.isNumber(args[4]);

        double x = noXDifference ? playerLocation.getX() : Double.parseDouble(args[0]);
        double y = noYDifference ? playerLocation.getY() : Double.parseDouble(args[1]);
        double z = noZDifference ? playerLocation.getZ() : Double.parseDouble(args[2]);

        float pitch = (hasPitch)
                ? ((args[3].equals("~")) ? playerLocation.getPitch() : Float.parseFloat(args[3]))
                : playerLocation.getPitch();
        float yaw = (hasYaw)
                ? ((args[4].equals("~")) ? playerLocation.getYaw() : Float.parseFloat(args[4]))
                : playerLocation.getYaw();

        Location location = new Location(player.getWorld(), x, y, z, yaw, pitch);
        CUtils.runSync(() -> player.teleport(location));
    }

    private void parseTpHere(Player sender, String[] args) {
        boolean hasArgs = args.length >= 1;

        if (!hasArgs) {
            CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "nickname"));

            return;
        }

        String targetName = args[0];

        doTeleport(sender, targetName);
    }

    private void parseTp(CommandSender sender, String[] args) {
        ServerManager svManager = Core.getInstance().getServerManager();
        PlayerManager manager = PlayerManager.getInstance();

        boolean tpOther = args.length >= 2;

        if (tpOther) {
            String who = args[0];
            String target = args[1];

            Player whoPl = Bukkit.getPlayerExact(who);
            Player targetPl = Bukkit.getPlayerExact(target);

            if (whoPl != null && targetPl != null) {
                CUtils.runSync(() -> whoPl.teleport(targetPl.getLocation()));

                CUtils.msg(sender, Lang.SUCCESS_COLOR + who + " was successfully teleported to " + target + ".");
            } else {
                Optional<String> targetServer = (targetPl != null)
                        ? Optional.of(manager.getDatabase(targetPl.getUniqueId()).getBungeeServer())
                        : svManager.getPlayerServer(target);

                boolean present = targetServer.isPresent();

                if (!present) {
                    CUtils.msg(sender, String.format(Lang.SPECIFIC_PLAYER_OFFLINE_FORMATABLE, target));

                    return;
                }

                boolean whoOnline = svManager.isPlayerOnline(who);

                if (!whoOnline) {
                    CUtils.msg(sender, String.format(Lang.SPECIFIC_PLAYER_OFFLINE_FORMATABLE, who));

                    return;
                }

                String svName = targetServer.get();
                new SendToServerAction(who, svName).push(JedisManager.getInstance().getPool());

                CUtils.msg(sender, Lang.SUCCESS_COLOR + who + " has been successfully teleported to the server " + svName + ".");
            }

        } else {
            if (isNotPlayer(sender)) {
                return;
            }

            Player player = (Player) sender;
            String target = args[0];

            Player targetPlayer = Bukkit.getPlayer(target);

            if (targetPlayer != null) {
                CUtils.runSync(() -> player.teleport(targetPlayer));

                return;
            }

            Optional<String> targetSv = Core.getInstance().getServerManager().getPlayerServer(target);
            boolean present = targetSv.isPresent();

            if (!present) {
                CUtils.msg(sender, Lang.PLAYER_OFFLINE);

                return;
            }

            new SendToServerAction(player.getName(), targetSv.get())
                    .push(JedisManager.getInstance().getPool());

            CUtils.msg(player, Lang.SUCCESS_COLOR + "You have been teleported to " + target + "'s server.");
        }

    }

    private void doTeleport(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target != null) {
            CUtils.runSync(() -> target.teleport(sender));

            return;
        }

        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(sender.getUniqueId());
        String playerServer = database.getBungeeServer();

        Optional<String> targetServer = Core.getInstance().getServerManager().getPlayerServer(targetName);
        boolean present = targetServer.isPresent();

        if (!present) {
            CUtils.msg(sender, Lang.PLAYER_OFFLINE);

            return;
        }

        new SendToServerAction(targetName, playerServer)
                .push(JedisManager.getInstance().getPool());

        CUtils.msg(sender, Lang.SUCCESS_COLOR + targetName + " was successfully sent to your server.");
    }

}
