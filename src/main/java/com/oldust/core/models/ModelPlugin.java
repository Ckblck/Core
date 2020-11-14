package com.oldust.core.models;

import com.oldust.core.Core;
import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import com.oldust.core.models.commands.ModelCommand;
import com.oldust.core.models.commands.ModelModifyCommand;
import lombok.Getter;
import uk.lewdev.standmodels.model.ModelManager;

import java.util.Collections;

@InheritedPlugin(name = "Models")
@Getter
public class ModelPlugin extends Plugin {
    private ModelManager modelManager;
    private ModelModifyCommand modelModifyCommand;

    @Override
    public void onEnable() { // TODO: RANKS PERMISSIONS
        modelManager = new ModelManager(Core.getInstance());
        new ModelCommand(this, "model", null);
        modelModifyCommand = new ModelModifyCommand(this, "modelmodify", Collections.singletonList("mmodify"));
    }

    @Override
    public void onDisable() {}

}
