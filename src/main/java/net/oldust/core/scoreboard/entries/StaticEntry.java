package net.oldust.core.scoreboard.entries;

import net.oldust.core.scoreboard.objects.PlayerScoreboard;
import net.oldust.core.scoreboard.objects.ScoreEntry;
import net.oldust.core.utils.CUtils;

/**
 * A StaticEntry is used when a unmodifiable/static line
 * is needed to be displayed in the scoreboard.
 */

public class StaticEntry extends ScoreEntry {
    private final String text;

    public StaticEntry(PlayerScoreboard playerScoreboard, String text) {
        super(playerScoreboard);

        this.text = CUtils.color(text);

        setEntry(create()); // after variable initialization
    }

    @Override
    public String getText() {
        return text;
    }

}
