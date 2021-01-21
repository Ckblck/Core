package net.oldust.core.staff.commands;

import net.oldust.core.inherited.commands.InheritedCommand;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.staff.StaffPlugin;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpeedCommand extends InheritedCommand<StaffPlugin> {

    public SpeedCommand(StaffPlugin plugin) {
        super(plugin, "speed", null);
    }

    @Override
    public TriConsumer<CommandSender, String, String[]> onCommand() {
        return (sender, label, args) -> {
            if (isNotPlayer(sender)) return;
            if (isNotAboveOrEqual(sender, PlayerRank.ADMIN)) return;

            if (args.length == 0) {
                CUtils.msg(sender, String.format(Lang.MISSING_ARGUMENT_FORMATABLE, "amount"), LangSound.ERROR);

                return;
            }

            String amountStr = args[0];
            boolean number = NumberUtils.isNumber(amountStr);

            if (!number) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "Wrong amount! 0-10", LangSound.ERROR);

                return;
            }

            Player player = (Player) sender;
            float amount = Float.parseFloat(args[0]) / 10;

            if (amount < 0.F || amount > 10.F) {
                CUtils.msg(sender, Lang.ERROR_COLOR + "Wrong amount! 0.0-10.0", LangSound.ERROR);

                return;
            }

            String noun;

            if (player.isFlying()) {
                noun = "flying";

                player.setFlySpeed(amount);
            } else {
                noun = "walking";

                player.setWalkSpeed(amount);
            }

            CUtils.msg(player, Lang.SUCCESS_COLOR_ALT + "Successfully set " + noun + " speed to " + (amount * 10) + ". '2.0' is the default walk/fly speed.");
        };
    }

}
