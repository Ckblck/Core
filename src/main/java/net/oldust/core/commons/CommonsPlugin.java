package net.oldust.core.commons;

import lombok.Getter;
import net.oldust.core.commons.commands.MsgCommand;
import net.oldust.core.commons.commands.PingCommand;
import net.oldust.core.commons.commands.ReplyCommand;
import net.oldust.core.commons.commands.ReportCommand;
import net.oldust.core.commons.login.LoginEvent;
import net.oldust.core.commons.reports.ReportsManager;
import net.oldust.core.commons.tab.TabListManager;
import net.oldust.core.inherited.plugins.InheritedPlugin;
import net.oldust.core.inherited.plugins.Plugin;
import net.oldust.core.scoreboard.entries.DynamicEntry;
import net.oldust.core.scoreboard.entries.StaticEntry;
import net.oldust.core.scoreboard.objects.Line;
import net.oldust.core.scoreboard.objects.PlayerScoreboard;
import org.apache.commons.lang.RandomStringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@InheritedPlugin(name = "Commons")
public class CommonsPlugin extends Plugin {
    private ReportsManager reportsManager;
    private TabListManager tabListManager;
    private MsgCommand msgCommand;

    @Override
    public void onEnable() {
        new ReportCommand(this);
        new PingCommand(this);
        new ReplyCommand(this);
        new LoginEvent();

        // todo remove
        PlayerScoreboard playerScoreboard = new PlayerScoreboard("Score", "#fcba03⚔ &r&lOLDUST #fcba03⚔");
        playerScoreboard.tryInsertAt(2, new StaticEntry(playerScoreboard, " "));

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        DynamicEntry build = DynamicEntry.builder(playerScoreboard)
                .line(Line.of(
                        "Hola {0}, Date: {1}",
                        "Cookieblack", format.format(new Date())
                ))
                .task(0, 20)
                .withProperty(property -> property.formatBunch(
                        RandomStringUtils.randomAlphabetic(5), format.format(new Date())
                ))
                .whenCancelled(DynamicEntry::removeLine)
                /*.cancelWhen(entry -> entry.getIteration() == 10)*/
                .build();

        playerScoreboard.insertEntryAt(1, new StaticEntry(playerScoreboard, " este deberia estar en 13"));

        playerScoreboard.addEntry(build);

        msgCommand = new MsgCommand(this);
        tabListManager = new TabListManager();
        reportsManager = new ReportsManager();
    }

    @Override
    public void onDisable() {

    }

}
