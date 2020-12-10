package com.oldust.core.staff.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.lambda.TriConsumer;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GamemodeCommand extends InheritedCommand<StaffPlugin> {

    public GamemodeCommand(StaffPlugin plugin) {
        super(plugin, "gm", List.of("gamemode", "gmc", "gms", "gmsp", "gma"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            if (!(sender instanceof Player)) {
                if (args.length < 2) {
                    isNotPlayer(sender);
                } else {
                    Player player = Bukkit.getPlayer(args[1]);
                    boolean connected = player != null;

                    if (!connected) {
                        CUtils.msg(sender, Lang.PLAYER_OFFLINE);

                        return;
                    }

                    setGamemode(sender, player, label, args);
                }

                return;
            }

            if (args.length < 2) {
                setGamemode(sender, ((Player) sender), label, args);
            } else {
                Player player = Bukkit.getPlayer(args[1]);
                boolean connected = player != null;

                if (!connected) {
                    CUtils.msg(sender, Lang.PLAYER_OFFLINE);

                    return;
                }

                setGamemode(sender, player, label, args);
            }

        };
    }

    private void setGamemode(CommandSender sender, Player player, String label, String[] args) {
        switch (label.toLowerCase()) {
            case "gmc":
                if (isNotAboveOrEqual(sender, PlayerRank.ADMIN)) return;

                player.setGameMode(GameMode.CREATIVE);

                break;
            case "gms":
                player.setGameMode(GameMode.SURVIVAL);

                break;
            case "gma":
                player.setGameMode(GameMode.ADVENTURE);

                break;
            case "gmsp":
                player.setGameMode(GameMode.SPECTATOR);

                break;
            default:
                if (args.length == 0) {
                    CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "0/1/2/3"));

                    return;
                }

                GameMode gameMode = null;
                String mode = args[0];
                boolean number = NumberUtils.isNumber(mode);

                if (number) {
                    gameMode = GameMode.getByValue(Integer.parseInt(mode));

                    if (gameMode == null) {
                        CUtils.msg(sender, Lang.ERROR_COLOR + "The specified gamemode is wrong. Available: 0/1/2/3.");

                        return;
                    }

                } else {
                    if (mode.equalsIgnoreCase("creative")
                            || mode.equalsIgnoreCase("survival")
                            || mode.equalsIgnoreCase("spectator")) {
                        gameMode = GameMode.valueOf(mode.toUpperCase());
                    }
                }

                assert gameMode != null;

                if (player.getGameMode() == gameMode) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "You already are in that gamemode!");

                    return;
                }

                if (gameMode == GameMode.CREATIVE && isNotAboveOrEqual(sender, PlayerRank.ADMIN)) return;

                player.setGameMode(gameMode);

                break;

        }

        CUtils.msg(sender, Lang.SUCCESS_COLOR_ALT + "Successfully switched gamemode.");
    }

}