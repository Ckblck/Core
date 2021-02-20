package net.oldust.core.actionbar;

import lombok.RequiredArgsConstructor;
import net.oldust.core.actionbar.components.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A class which holds
 * the current line of a player's
 * actionbar.
 */

@RequiredArgsConstructor
public class Actionbar {
    private final List<Component> components = new ArrayList<>();
    private final UUID uuid;

    public void update() {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null)
            return;

        player.sendActionBar();
    }

    public String build() {
        StringBuilder build = new StringBuilder();

        for (Component component : components) {
            build.append(component.build());
        }

        return build.toString();
    }

}
