package com.oldust.core.commons;

import com.oldust.core.commons.commands.GamemodeCommand;
import com.oldust.core.commons.commands.MsgCommand;
import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;

@InheritedPlugin(name = "Commons")
public class CommonsPlugin extends Plugin {

    @Override
    public void onEnable() {
        new MsgCommand(this);
        new GamemodeCommand(this);
    }

    @Override
    public void onDisable() {

    }

}
