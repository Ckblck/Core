package net.oldust.core.models;

import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.inherited.plugins.InheritedPlugin;
import net.oldust.core.inherited.plugins.Plugin;
import net.oldust.core.models.commands.ModelCommand;
import net.oldust.core.models.commands.ModelModifyCommand;
import uk.lewdev.standmodels.StandModelLib;

import java.util.Collections;

@Getter
@InheritedPlugin(name = "Models")
public class ModelPlugin extends Plugin {
    private StandModelLib standModelLib;
    private ModelModifyCommand modelModifyCommand;

    @Override
    public void onEnable() {
        standModelLib = new StandModelLib(Core.getInstance());
        new ModelCommand(this, "model", null);
        modelModifyCommand = new ModelModifyCommand(this, "mmodify", Collections.singletonList("modelmodify"));
    }

    @Override
    public void onDisable() {
    }

}
