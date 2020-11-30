package com.oldust.core.staff;

import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import com.oldust.core.staff.chest.FakeChestsManager;
import com.oldust.core.staff.commands.FindCommand;
import com.oldust.core.staff.commands.SendCommand;
import com.oldust.core.staff.commands.StaffChatCommand;
import com.oldust.core.staff.commands.TeleportCommand;
import com.oldust.core.staff.dispatch.DispatchCommand;
import com.oldust.core.staff.mode.StaffModeManager;
import com.oldust.core.staff.mode.command.ModeCommand;
import com.oldust.core.staff.mode.command.VanishCommand;
import lombok.Getter;

@Getter
@InheritedPlugin(name = "Staff")
public class StaffPlugin extends Plugin {
    private FakeChestsManager fakeChestsManager;
    private StaffModeManager staffModeManager;

    @Override
    public void onEnable() {
        new DispatchCommand(this);
        new ModeCommand(this);
        new VanishCommand(this);
        new StaffChatCommand(this);
        new TeleportCommand(this);
        new FindCommand(this);
        new SendCommand(this);

        fakeChestsManager = new FakeChestsManager();
        staffModeManager = new StaffModeManager();
    }

    @Override
    public void onDisable() {

    }

}
