package net.oldust.core.staff;

import lombok.Getter;
import net.oldust.core.inherited.plugins.InheritedPlugin;
import net.oldust.core.inherited.plugins.Plugin;
import net.oldust.core.staff.chest.FakeChestsManager;
import net.oldust.core.staff.commands.*;
import net.oldust.core.staff.mode.StaffModeManager;
import net.oldust.core.staff.mode.command.ModeCommand;
import net.oldust.core.staff.mode.command.VanishCommand;

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
        new LogCommand(this);
        new BanCommand(this);
        new UnbanCommand(this);
        new KickCommand(this);
        new MuteCommand(this);
        new UnmuteCommand(this);
        new PlayerDataCommand(this);
        new NoMpsCommand(this);
        new GamemodeCommand(this);
        new ClearInventoryCommand(this);

        fakeChestsManager = new FakeChestsManager();
        staffModeManager = new StaffModeManager();
    }

    @Override
    public void onDisable() {

    }

}
