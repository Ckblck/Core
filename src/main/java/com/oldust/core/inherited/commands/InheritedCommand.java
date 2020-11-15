package com.oldust.core.inherited.commands;

import com.oldust.core.Core;
import com.oldust.core.inherited.plugins.Plugin;
import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.Lang;
import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class InheritedCommand<T extends Plugin> extends Command {
    private final BiConsumer<CommandSender, String[]> commandConsumer;
    @Getter
    private final T plugin;

    public InheritedCommand(T plugin, String name, @Nullable List<String> aliases) {
        super(name);

        Core core = Core.getInstance();
        core.getServer().getCommandMap().register(plugin.getName(), this);

        this.commandConsumer = onCommand();
        this.plugin = plugin;

        if (aliases != null)
            setAliases(aliases);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        try {
            commandConsumer.accept(sender, args);
        } catch (Throwable t) {
            CUtils.msg(sender, Lang.ERROR_COLOR + "An error occurred while executing the command. " + t.getMessage());
            t.printStackTrace();

            return false;
        }

        return true;
    }

    public abstract BiConsumer<CommandSender, String[]> onCommand();

    public boolean isNotPlayer(CommandSender sender) {
        boolean notPlayer = !(sender instanceof Player);

        if (notPlayer) {
            CUtils.msg(sender, Lang.MUST_BE_PLAYER);
        }

        return notPlayer;
    }

}
