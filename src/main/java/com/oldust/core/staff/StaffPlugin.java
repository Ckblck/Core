package com.oldust.core.staff;

import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import com.oldust.core.staff.dispatch.DispatchCommand;
import com.oldust.core.staff.mode.command.ModeCommand;

@InheritedPlugin(name = "Staff")
public class StaffPlugin extends Plugin {

    @Override
    public void onEnable() {
        new DispatchCommand(this);
        new ModeCommand(this);
    }

    @Override
    public void onDisable() {

    }

}
