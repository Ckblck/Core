package com.oldust.core.commons.commands;

import com.oldust.core.commons.CommonsPlugin;
import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.ranks.PlayerRank;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import com.oldust.core.utils.lambda.TriConsumer;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GamemodeCommand extends InheritedCommand<CommonsPlugin> {

    public GamemodeCommand(CommonsPlugin plugin) {
        super(plugin, "gm", List.of("gamemode", "gmc", "gms", "gmsp", "gma"));
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;
            if (isNotAboveOrEqual(sender, PlayerRank.MOD)) return;

            Player player = (Player) sender;

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
        };
    }

}
