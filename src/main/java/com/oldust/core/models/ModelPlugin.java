package com.oldust.core.models;

import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import com.oldust.core.models.commands.ModelCommand;

@InheritedPlugin(name = "Models")
public class ModelPlugin extends Plugin {

    @Override
    public void onEnable() {
        new ModelCommand(this, "model", null);
    }

    @Override
    public void onDisable() {

    }

}
