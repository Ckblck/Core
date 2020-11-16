package com.oldust.core;

import com.oldust.core.inherited.plugins.InheritedPluginsManager;
import com.oldust.core.models.ModelPlugin;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.utils.CUtils;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
    @Getter
    private static Core instance;

    @Override
    public void onEnable() {
        CUtils.inform("Core", "Inicializando core...");
        instance = this;

        new MySQLManager();

        InheritedPluginsManager.loadInheritedPlugin(ModelPlugin.class);
        InheritedPluginsManager.onEnable();
    }

    @Override
    public void onDisable() {
        InheritedPluginsManager.onDisable();
    }

}
