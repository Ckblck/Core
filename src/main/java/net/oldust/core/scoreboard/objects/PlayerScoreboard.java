package net.oldust.core.scoreboard.objects;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.oldust.core.utils.CUtils;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A PlayerScoreboard is a scoreboard
 * that a player can have.
 */

@Getter
public class PlayerScoreboard {
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Objective objective;
    private final Map<Integer, ScoreboardEntry<?>> lines = new HashMap<>();

    public PlayerScoreboard(String scoreboardName, String title) {
        this.objective = scoreboard.registerNewObjective(scoreboardName, "#$%#$^", CUtils.color(title));

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * Adds an entry to the scoreboard.
     * If there is already a line at the specified line,
     * this entry will be inserted at the LAST LINE of the scoreboard.
     *
     * @param lineNumber line in which the entry will be inserted,
     *                   if 1 is passed, the line will be the first
     * @param entry      entry to add
     */

    public void tryInsertAt(int lineNumber, ScoreboardEntry<?> entry) {
        Preconditions.checkArgument(lineNumber > 0, "lineNumber cannot be less or equal than 0.");

        int score = 16 - lineNumber;

        if (lines.containsKey(score)) { // Already exists a line at that number, place right after the next available.
            for (int i = score - 1; i > 0; i--) {
                if (lines.containsKey(i)) continue;

                score = i;

                break;
            }
        }

        setAt(entry, score);
    }

    /**
     * Inserts an entry AT A SPECIFIC LINE.
     * If already exists a line there, all lines below the specified
     * will be moved down.
     *
     * @param lineNumber line number to set the entry at
     * @param entry      entry to set
     */

    public void insertEntryAt(int lineNumber, ScoreboardEntry<?> entry) {
        Preconditions.checkArgument(lineNumber > 0, "lineNumber cannot be less or equal than 0.");

        int score = 16 - lineNumber;

        if (lines.containsKey(score)) {
            for (int i = score; i > 0; i--) { // Move every entry one line down
                ScoreboardEntry<?> lineEntry = lines.remove(i);

                if (lineEntry != null) {
                    int lineBelow = i - 1;

                    lineEntry.getScoreObj().setScore(lineBelow);
                    lines.put(lineBelow, lineEntry);
                }
            }
        }

        setAt(entry, score);
    }

    /**
     * Adds an entry to the scoreboard
     * at the next line.
     *
     * @param entry entry to add
     */

    public void addEntry(ScoreboardEntry<?> entry) {
        int lastEntry = Collections.min(lines.keySet());

        tryInsertAt(lastEntry + 1, entry);
    }

    /**
     * Sets an entry at a specific line.
     * If the line is already used, it will be
     * replaced with this new entry.
     *
     * @param lineNumber line number to set the entry at
     * @param entry      entry to set
     */

    public void setEntryAt(int lineNumber, ScoreboardEntry<?> entry) {
        Preconditions.checkArgument(lineNumber > 0, "lineNumber cannot be less or equal than 0.");

        int score = 16 - lineNumber;

        if (lines.containsKey(score)) {
            removeEntry(lines.get(score));
        }

        setAt(entry, score);
    }

    /**
     * Verifies if an entry exists
     * at a specified line number.
     *
     * @param lineNumber line to verify
     * @return true if there is an entry at that line
     */

    public boolean isEntryAt(int lineNumber) {
        Preconditions.checkArgument(lineNumber > 0, "lineNumber cannot be less or equal than 0.");

        int score = 16 - lineNumber;

        return lines.containsKey(score);
    }

    /**
     * Removes an entry from the scoreboard,
     * resulting in the line being also deleted.
     *
     * @param entry entry to remove
     */

    public void removeEntry(ScoreboardEntry<?> entry) {
        scoreboard.resetScores(entry.getScoreObj().getEntry());
        lines.values().remove(entry);
    }

    private void setAt(ScoreboardEntry<?> entry, int score) {
        Score scoreObj = entry.getScoreObj();
        scoreObj.setScore(score);

        lines.put(score, entry);
    }

}
