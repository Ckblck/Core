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
import net.oldust.core.scoreboard.Line;
import net.oldust.core.scoreboard.ServerScoreboard;
import net.oldust.core.scoreboard.entries.DynamicEntry;
import net.oldust.core.scoreboard.entries.StaticEntry;
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
        ServerScoreboard serverScoreboard = new ServerScoreboard("Score", "Oldust");
        serverScoreboard.addEntry(new StaticEntry(serverScoreboard, " "));
        serverScoreboard.addEntry(new StaticEntry(serverScoreboard, "Entrada"));
        serverScoreboard.addEntry(new StaticEntry(serverScoreboard, "234 "));

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        DynamicEntry build = DynamicEntry.builder()
                .line(Line.of(
                        "Hola {0}, Date: {1}",
                        "Cookieblack", format.format(new Date()))
                )
                .task(0, 20)
                .withProperty(property -> property.formatBunch(
                        RandomStringUtils.randomAlphabetic(5), format.format(new Date())
                ))
                .whenCancelled(entry -> entry.getServerScoreboard().removeEntry(entry))
                .cancelWhen(entry -> entry.getIteration() == 10)
                .scoreboard(serverScoreboard)
                .build();

        serverScoreboard.addEntry(build);

        serverScoreboard.addEntry(10, new StaticEntry(serverScoreboard, " este deberia estar en 10"));

        msgCommand = new MsgCommand(this);
        tabListManager = new TabListManager();
        reportsManager = new ReportsManager();
    }

    @Override
    public void onDisable() {

    }

}
