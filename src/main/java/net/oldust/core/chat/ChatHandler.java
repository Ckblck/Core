package net.oldust.core.chat;

import net.oldust.core.chat.event.ChatListener;
import net.oldust.core.inherited.plugins.InheritedPlugin;
import net.oldust.core.inherited.plugins.Plugin;
import net.oldust.core.utils.CUtils;

@InheritedPlugin(name = "ChatHandler")
public class ChatHandler extends Plugin {

    @Override
    public void onEnable() {
        CUtils.registerEvents(new ChatListener());
    }

    @Override
    public void onDisable() {

    }

}
