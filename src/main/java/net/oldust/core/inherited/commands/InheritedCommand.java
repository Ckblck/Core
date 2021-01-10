package net.oldust.core.inherited.commands;

import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.inherited.plugins.Plugin;
import net.oldust.core.ranks.PlayerRank;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lambda.TriConsumer;
import net.oldust.core.utils.lang.Lang;
import net.oldust.core.utils.lang.LangSound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public abstract class InheritedCommand<T extends Plugin> extends Command {
    private final TriConsumer<CommandSender, String, String[]> commandConsumer;
    @Getter
    private final T plugin;

    public InheritedCommand(T plugin, String name, @Nullable List<String> aliases) {
        super(name);

        this.commandConsumer = onCommand();
        this.plugin = plugin;

        if (aliases != null)
            setAliases(aliases);

        Core core = Core.getInstance();
        core.getServer().getCommandMap().register(plugin.getName(), this);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        try {
            commandConsumer.accept(sender, label, args);
        } catch (Throwable t) {
            CUtils.msg(sender, Lang.ERROR_COLOR + "An error occurred while executing the command. " + t.getMessage(), LangSound.ERROR);
            t.printStackTrace();

            return false;
        }

        return true;
    }

    public abstract TriConsumer<CommandSender, String, String[]> onCommand();

    public boolean isNotPlayer(CommandSender sender) {
        boolean notPlayer = !(sender instanceof Player);

        if (notPlayer) {
            CUtils.msg(sender, Lang.MUST_BE_PLAYER);
        }

        return notPlayer;
    }

    public boolean isNotAboveOrEqual(CommandSender sender, PlayerRank senderRank, PlayerRank neededRank) {
        if (!senderRank.isEqualOrHigher(neededRank)) {
            CUtils.msg(sender, Lang.NO_PERMISSIONS, LangSound.ERROR);

            return true;
        }

        return false;
    }

    public boolean isNotAboveOrEqual(CommandSender sender, PlayerRank rank) {
        boolean notAbove = !PlayerRank.getPlayerRank(sender).isEqualOrHigher(rank);

        if (notAbove) {
            CUtils.msg(sender, Lang.NO_PERMISSIONS, LangSound.ERROR);
        }

        return notAbove;
    }

    public boolean isNotStaff(CommandSender sender) {
        boolean notStaff = !PlayerRank.getPlayerRank(sender).isStaff();

        if (notStaff) {
            CUtils.msg(sender, Lang.NO_PERMISSIONS, LangSound.ERROR);
        }

        return notStaff;
    }

}
