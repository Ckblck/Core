package com.oldust.core.models;

import com.oldust.core.Core;
import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import com.oldust.core.models.commands.ModelCommand;
import lombok.Getter;
import uk.lewdev.standmodels.model.ModelManager;

@InheritedPlugin(name = "Models")
public class ModelPlugin extends Plugin {
    @Getter private ModelManager modelManager;

    @Override
    public void onEnable() {
        modelManager = new ModelManager(Core.getInstance());
        new ModelCommand(this, "model", null);
    }

    @Override
    public void onDisable() {

    }

}
