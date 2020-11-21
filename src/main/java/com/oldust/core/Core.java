package com.oldust.core;

import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.chat.ChatHandler;
import com.oldust.core.inherited.plugins.InheritedPluginsManager;
import com.oldust.core.models.ModelPlugin;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.ranks.permission.PermissionsManager;
import com.oldust.core.utils.CUtils;
import com.oldust.sync.JedisManager;
import com.oldust.sync.PlayerManager;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
    @Getter
    private static Core instance;

    @Override
    public void onEnable() {
        CUtils.inform("Core", "Inicializando core...");
        instance = this;

        new JedisManager();
        new MySQLManager();
        new PlayerManager();
        new ActionsReceiver();
        new PermissionsManager();

        InheritedPluginsManager.loadInheritedPlugin(ModelPlugin.class);
        InheritedPluginsManager.loadInheritedPlugin(ChatHandler.class);
        InheritedPluginsManager.loadInheritedPlugin(PermissionsManager.class);

        InheritedPluginsManager.onEnable();
    }

    @Override
    public void onDisable() {
        InheritedPluginsManager.onDisable();
    }
}
