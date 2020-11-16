package com.oldust.core.chat;

import com.oldust.core.chat.event.ChatListener;
import com.oldust.core.inherited.plugins.InheritedPlugin;
import com.oldust.core.inherited.plugins.Plugin;
import com.oldust.core.utils.CUtils;

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
