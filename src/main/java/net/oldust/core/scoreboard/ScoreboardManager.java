package net.oldust.core.scoreboard;

import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.internal.provider.EventsProvider;
import net.oldust.core.scoreboard.objects.PlayerScoreboard;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {
    private final Map<UUID, PlayerScoreboard> scoreboards = new HashMap<>();
    @Getter
    private static ScoreboardManager instance;

    public ScoreboardManager() {
        instance = this;

        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(PlayerQuitEvent.class, (ev, db)
                -> scoreboards.remove(ev.getPlayer().getUniqueId()));
    }

    /**
     * Creates an scoreboard for a player.
     *
     * @param player          player to create the scoreboard for
     * @param scoreboardName  name of the scoreboard, can be anything
     * @param scoreboardTitle title of the scoreboard (displayed on the first, centered line)
     */

    public void createScoreboard(UUID player, String scoreboardName, String scoreboardTitle) {
        scoreboards.put(player, new PlayerScoreboard(scoreboardName, scoreboardTitle));
    }

    /**
     * Gets the scoreboard of a player.
     */

    public PlayerScoreboard getScoreboard(UUID player) {
        return scoreboards.get(player);
    }

    /**
     * Verifies if a player has a scoreboard.
     */

    public boolean hasScoreboard(Player player) {
        return hasScoreboard(player.getUniqueId());
    }

    /**
     * Verifies if a player has a scoreboard.
     */

    public boolean hasScoreboard(UUID player) {
        return scoreboards.containsKey(player);
    }

}
