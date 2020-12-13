package net.oldust.core.commons;

import lombok.Getter;
import net.oldust.core.commons.commands.MsgCommand;
import net.oldust.core.commons.commands.PingCommand;
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

    @Override
    public void onEnable() {
        new MsgCommand(this);
        new ReportCommand(this);
        new PingCommand(this);

        tabListManager = new TabListManager();
        reportsManager = new ReportsManager();
    }

    @Override
    public void onDisable() {

    }

}
