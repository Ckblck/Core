package com.oldust.core.commons;

import com.oldust.core.commons.commands.MsgCommand;
import com.oldust.core.commons.commands.ReportCommand;
import com.oldust.core.commons.reports.ReportsManager;
import com.oldust.core.commons.tab.TabListManager;
import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import lombok.Getter;

@Getter
@InheritedPlugin(name = "Commons")
public class CommonsPlugin extends Plugin {
    private ReportsManager reportsManager;
    private TabListManager tabListManager;

    @Override
    public void onEnable() {
        new MsgCommand(this);
        new ReportCommand(this);

        tabListManager = new TabListManager();
        reportsManager = new ReportsManager();
    }

    @Override
    public void onDisable() {

    }

}
