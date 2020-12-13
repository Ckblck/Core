package net.oldust.core.commons;

import lombok.Getter;
import net.oldust.core.commons.commands.MsgCommand;
import net.oldust.core.commons.commands.PingCommand;
import net.oldust.core.commons.commands.ReplyCommand;
import net.oldust.core.commons.commands.ReportCommand;
import net.oldust.core.commons.reports.ReportsManager;
import net.oldust.core.commons.tab.TabListManager;
import net.oldust.core.inherited.plugins.InheritedPlugin;
import net.oldust.core.inherited.plugins.Plugin;

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

        msgCommand = new MsgCommand(this);
        tabListManager = new TabListManager();
        reportsManager = new ReportsManager();
    }

    @Override
    public void onDisable() {

    }

}
