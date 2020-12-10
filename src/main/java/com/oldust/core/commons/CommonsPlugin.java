package com.oldust.core.commons;

import com.oldust.core.commons.commands.MsgCommand;
import com.oldust.core.commons.reports.ReportsManager;
import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import lombok.Getter;

@Getter
@InheritedPlugin(name = "Commons")
public class CommonsPlugin extends Plugin {
    private ReportsManager reportsManager;

    @Override
    public void onEnable() {
        new MsgCommand(this);

        reportsManager = new ReportsManager();
    }

    @Override
    public void onDisable() {

    }

}
