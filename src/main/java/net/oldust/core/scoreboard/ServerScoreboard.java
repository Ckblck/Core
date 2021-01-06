package net.oldust.core.scoreboard;

import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.internal.provider.EventsProvider;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

@Getter
public class ServerScoreboard {
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Objective objective;

    private int score = 15;

    public ServerScoreboard(String scoreboardName, String title) {
        this.objective = scoreboard.registerNewObjective(scoreboardName, "#$%#$^", title);

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(PlayerJoinEvent.class, (event, database) ->
                event.getPlayer().setScoreboard(scoreboard));
    }

    /**
     * Adds an entry to the scoreboard with
     * an specific score.
     *
     * @param score score the entry will have
     * @param entry entry to add
     */

    public void addEntry(int score, ScoreboardEntry<?> entry) {
        if (score == -1) return;

        Score scoreObj = entry.getScoreObj();

        scoreObj.setScore(score);
    }

    /**
     * Adds an entry to the scoreboard
     * at the next line.
     *
     * @param entry entry to add
     */

    public void addEntry(ScoreboardEntry<?> entry) {
        addEntry(getScore(), entry);
    }

    /**
     * Removes an entry from the scoreboard,
     * resulting in the line being also deleted.
     *
     * @param entry entry to remove
     */

    public void removeEntry(ScoreboardEntry<?> entry) {
        scoreboard.resetScores(entry.getScoreObj().getEntry());
        score++;
    }

    /**
     * Gets the score of the next entry
     * in a descending order.
     *
     * @return -1 if there is no place left
     */

    public int getScore() {
        if (score == 0) return -1;

        return score--;
    }

}
