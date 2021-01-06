package net.oldust.core.scoreboard;

import lombok.Getter;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * TeamEntry is used for dynamic lines, which need
 * to be updated periodically. That is why {@link Team} are used.
 */

public abstract class TeamEntry extends ScoreboardEntry<Team> {
    @Getter
    private String entryName;

    protected TeamEntry(ServerScoreboard serverScoreboard) {
        super(serverScoreboard);
    }

    @Override
    protected Team create() {
        ServerScoreboard serverScoreboard = getServerScoreboard();
        Scoreboard scoreboard = serverScoreboard.getScoreboard();

        String entryName = getRandomName();

        Team team = scoreboard.registerNewTeam(entryName);

        team.addEntry(entryName);
        team.setPrefix(getText());

        this.entryName = entryName;

        return team;
    }

}
