package net.oldust.core.staff.commands;

import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
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
            PlayerRank rank = PlayerRank.getPlayerRank(sender);

            if (isNotAboveOrEqual(sender, rank, PlayerRank.MOD)) return;

            if (!(sender instanceof Player)) {
                if (args.length < 2) {
                    isNotPlayer(sender);
                } else {
                    Player player = Bukkit.getPlayer(args[1]);
                    boolean connected = player != null;

                    if (!connected) {
                        CUtils.msg(sender, Lang.PLAYER_OFFLINE, LangSound.ERROR);

                        return;
                    }

                    setGamemode(sender, rank, player, label, args);
                }

                return;
            }

            if (args.length < 2) {
                setGamemode(sender, rank, ((Player) sender), label, args);
            } else {
                Player player = Bukkit.getPlayer(args[1]);
                boolean connected = player != null;

                if (!connected) {
                    CUtils.msg(sender, Lang.PLAYER_OFFLINE, LangSound.ERROR);

                    return;
                }

                setGamemode(sender, rank, player, label, args);
            }

        };
    }

    private void setGamemode(CommandSender sender, PlayerRank senderRank, Player player, String label, String[] args) {
        switch (label.toLowerCase()) {
            case "gmc":
                if (isNotAboveOrEqual(sender, senderRank, PlayerRank.ADMIN)) return;

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
                    CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATTABLE, "0/1/2/3", LangSound.ERROR));

                    return;
                }

                GameMode gameMode;
                String mode = args[0];
                boolean number = NumberUtils.isNumber(mode);

                if (number) {
                    gameMode = GameMode.getByValue(Integer.parseInt(mode));

                    if (gameMode == null) {
                        CUtils.msg(sender, Lang.ERROR_COLOR + "The specified gamemode is wrong. Available: 0/1/2/3.", LangSound.ERROR);

                        return;
                    }

                } else if (mode.equalsIgnoreCase("creative")
                        || mode.equalsIgnoreCase("survival")
                        || mode.equalsIgnoreCase("spectator")
                        || mode.equalsIgnoreCase("adventure")) {

                    gameMode = GameMode.valueOf(mode.toUpperCase());
                } else {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "The specified gamemode is wrong. Available: creative/survival/spectator/adventure.", LangSound.ERROR);

                    return;
                }

                if (player.getGameMode() == gameMode) {
                    CUtils.msg(sender, Lang.ERROR_COLOR + "You already are in that gamemode!", LangSound.ERROR);

                    return;
                }

                if (gameMode == GameMode.CREATIVE && isNotAboveOrEqual(sender, senderRank, PlayerRank.ADMIN)) {
                    return;
                }

                GameMode finalGameMode = gameMode;
                player.setGameMode(finalGameMode);

                break;

        }

        CUtils.msg(sender, Lang.SUCCESS_COLOR_ALT + "Successfully switched gamemode.");
    }

}
