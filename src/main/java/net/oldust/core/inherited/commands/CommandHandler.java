package net.oldust.core.inherited.commands;

import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lang.Lang;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

public interface CommandHandler {
    BiConsumer<CommandSender, String[]> handle();

    default boolean validatePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            CUtils.logConsole(Lang.MUST_BE_PLAYER);

            return false;
        }

        return true;
    }

}
