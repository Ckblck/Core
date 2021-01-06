package net.oldust.core.scoreboard;

import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public abstract class ScoreEntry extends ScoreboardEntry<Score> {

    public ScoreEntry(ServerScoreboard serverScoreboard) {
        super(serverScoreboard);
    }

    @Override
    protected Score create() {
        ServerScoreboard serverScoreboard = getServerScoreboard();
        Objective objective = serverScoreboard.getObjective();

        String entry = getText() + getRandomName();

        return objective.getScore(entry);
    }

}
