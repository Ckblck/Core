package net.oldust.core.models;

import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.inherited.plugins.InheritedPlugin;
import net.oldust.core.inherited.plugins.Plugin;
import net.oldust.core.models.commands.ModelCommand;
import net.oldust.core.models.commands.ModelModifyCommand;
import net.oldust.core.models.items.commands.ItemModelCommand;
import net.oldust.core.models.items.commands.ItemModelModifyCommand;
import uk.lewdev.standmodels.StandModelLib;

import java.util.Collections;

@Getter
@InheritedPlugin(name = "Models")
public class ModelPlugin extends Plugin {
    private StandModelLib standModelLib;
    private ModelModifyCommand modelModifyCommand;
    private ItemModelModifyCommand itemModelModifyCommand;

    @Override
    public void onEnable() {
        standModelLib = new StandModelLib(Core.getInstance());
        new ModelCommand(this, "model", null);
        modelModifyCommand = new ModelModifyCommand(this, "mmodify", Collections.singletonList("modelmodify"));

        new ItemModelCommand(this, "itemmodel", Collections.singletonList("im"));
        itemModelModifyCommand = new ItemModelModifyCommand(this, "immodify", Collections.singletonList("itemmodelmodify"));

    }

    @Override
    public void onDisable() {
    }

}
