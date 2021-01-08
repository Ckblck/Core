package net.oldust.core.scoreboard.objects;

import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

import java.util.concurrent.ThreadLocalRandom;

@Getter
public abstract class ScoreboardEntry<T> {
    private final PlayerScoreboard playerScoreboard;
    @Setter
    private T entry;

    protected ScoreboardEntry(PlayerScoreboard playerScoreboard) {
        this.playerScoreboard = playerScoreboard;
    }

    public abstract String getText();

    protected abstract T create();

    protected String getRandomName() {
        int colorAmount = ThreadLocalRandom.current().nextInt(4, 7);
        ChatColor[] colors = ChatColor.values();

        StringBuilder builder = new StringBuilder(colorAmount);

        for (int i = 0; i < colorAmount; i++) {
            ChatColor color = colors[ThreadLocalRandom.current().nextInt(colors.length)];

            builder.append(color);
        }

        return builder.toString();
    }

    public Score getScoreObj() {
        if (entry instanceof Score) {
            return (Score) entry;
        } else {
            Team team = (Team) entry;
            Objective objective = getPlayerScoreboard().getObjective();

            return objective.getScore(team.getName());
        }
    }

}
