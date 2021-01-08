package net.oldust.core.scoreboard.objects;

import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public abstract class ScoreEntry extends ScoreboardEntry<Score> {

    public ScoreEntry(PlayerScoreboard playerScoreboard) {
        super(playerScoreboard);
    }

    @Override
    protected Score create() {
        PlayerScoreboard playerScoreboard = getPlayerScoreboard();
        Objective objective = playerScoreboard.getObjective();

        String entry = getText() + getRandomName();

        return objective.getScore(entry);
    }

}
