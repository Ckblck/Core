package net.oldust.core.scoreboard.entries;

import net.oldust.core.scoreboard.ScoreEntry;
import net.oldust.core.scoreboard.ServerScoreboard;

/**
 * A StaticEntry is used when a non-changing line
 * is needed to be displayed in the scoreboard.
 */

public class StaticEntry extends ScoreEntry {
    private final String text;

    public StaticEntry(ServerScoreboard serverScoreboard, String text) {
        super(serverScoreboard);

        this.text = text;

        setEntry(create()); // after variable initialization
    }

    @Override
    public String getText() {
        return text;
    }

}
