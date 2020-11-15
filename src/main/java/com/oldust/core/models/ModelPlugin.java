package com.oldust.core.models;

import com.oldust.core.Core;
import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import com.oldust.core.models.commands.ModelCommand;
import com.oldust.core.models.commands.ModelModifyCommand;
import lombok.Getter;
import uk.lewdev.standmodels.StandModelLib;

import java.util.Collections;

@InheritedPlugin(name = "Models")
@Getter
public class ModelPlugin extends Plugin {
    private StandModelLib standModelLib;
    private ModelModifyCommand modelModifyCommand;

    @Override
    public void onEnable() { // TODO: RANKS PERMISSIONS
        standModelLib = new StandModelLib(Core.getInstance());
        new ModelCommand(this, "model", null);
        modelModifyCommand = new ModelModifyCommand(this, "modelmodify", Collections.singletonList("mmodify"));
    }

    @Override
    public void onDisable() {
    }

}
