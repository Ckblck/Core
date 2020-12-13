package net.oldust.core.interactive.components;

import net.oldust.core.Core;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Un InteractiveComponent es aquel
 * que ejecuta un bloque de código
 * a partir de la interacción
 * con un {@link net.md_5.bungee.api.chat.TextComponent}}.
 */

public class InteractiveComponent {
    private final Consumer<Player> click;

    public InteractiveComponent(Consumer<Player> click) {
        this.click = click;
    }

    public String create() {
        String randomCommand = RandomStringUtils.randomAlphanumeric(8) + "-(oldust-interactive-command)";
        CommandMap commandMap = Core.getInstance().getServer().getCommandMap();

        commandMap.register(randomCommand, new Command(randomCommand) {
            @Override
            public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
                click.accept(((Player) sender));

                return true;
            }
        });

        return randomCommand;
    }

}
