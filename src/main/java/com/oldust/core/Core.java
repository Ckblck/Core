package com.oldust.core;

import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.chat.ChatHandler;
import com.oldust.core.inherited.plugins.InheritedPluginsManager;
import com.oldust.core.models.ModelPlugin;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.ranks.permission.PermissionsManager;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.sync.JedisManager;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.ServerManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

public class Core extends JavaPlugin {
    @Getter
    private static Core instance;
    @Getter
    @Setter
    private String serverName;

    @Override
    public void onEnable() {
        instance = this;

        CUtils.inform("Core", "Inicializando core...");

        new JedisManager();
        new MySQLManager();
        new PlayerManager();
        new ServerManager();
        new ActionsReceiver();
        new PermissionsManager();

        InheritedPluginsManager.loadInheritedPlugin(ModelPlugin.class);
        InheritedPluginsManager.loadInheritedPlugin(ChatHandler.class);
        InheritedPluginsManager.loadInheritedPlugin(PermissionsManager.class);
        InheritedPluginsManager.loadInheritedPlugin(StaffPlugin.class);

        InheritedPluginsManager.onEnable();

        CUtils.inform("Core", "Iniciado con éxito.");
    }

    @Override
    public void onDisable() {
        ServerManager.getInstance().remove();
        InheritedPluginsManager.onDisable();
    }
}
