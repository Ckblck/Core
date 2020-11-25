package com.oldust.core.staff;

import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import com.oldust.core.staff.chest.FakeChestsManager;
import com.oldust.core.staff.dispatch.DispatchCommand;
import com.oldust.core.staff.mode.command.ModeCommand;
import com.oldust.core.staff.vanish.VanishHandler;
import com.oldust.core.staff.vanish.command.VanishCommand;
import lombok.Getter;

@Getter
@InheritedPlugin(name = "Staff")
public class StaffPlugin extends Plugin {
    private FakeChestsManager fakeChestsManager;
    private VanishHandler vanishHandler;

    @Override
    public void onEnable() {
        new DispatchCommand(this);
        new ModeCommand(this);
        new VanishCommand(this);

        fakeChestsManager = new FakeChestsManager();
        vanishHandler = new VanishHandler();
    }

    @Override
    public void onDisable() {

    }

}
