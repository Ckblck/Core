package com.oldust.core.models.commands;

import com.oldust.core.inherited.commands.InheritedCommand;
import com.oldust.core.inherited.plugins.Plugin;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

public class ModelCommand extends InheritedCommand {

    public ModelCommand(Plugin plugin, String name, @Nullable List<String> aliases) {
        super(plugin, name, aliases);
    }

    @Override
    public BiConsumer<CommandSender, String[]> onCommand() {
        return (sender, args) -> {
            sender.sendMessage("test");
        };
    }

}
