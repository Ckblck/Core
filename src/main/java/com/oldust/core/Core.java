package com.oldust.core;

import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.chat.ChatHandler;
import com.oldust.core.commons.EventsProvider;
import com.oldust.core.inherited.plugins.InheritedPluginsManager;
import com.oldust.core.interactive.panels.InteractivePanelManager;
import com.oldust.core.models.ModelPlugin;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.ranks.permission.PermissionsManager;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.sync.JedisManager;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.ServerManager;
import fr.minuskube.inv.InventoryManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Core extends JavaPlugin {
    @Getter
    private static Core instance;

    @Setter
    private String serverName;
    private InventoryManager inventoryManager;
    private ServerManager serverManager;
    private EventsProvider eventsProvider;

    @Override
    public void onEnable() {
        instance = this;

        long start = System.currentTimeMillis();
        CUtils.inform("Core", "Initializing core...");

        new JedisManager();
        new MySQLManager();

        eventsProvider = new EventsProvider();

        new PlayerManager();
        new ActionsReceiver();
        new PermissionsManager();

        serverManager = new ServerManager();
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        new InteractivePanelManager();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        InheritedPluginsManager.loadInheritedPlugin(ModelPlugin.class);
        InheritedPluginsManager.loadInheritedPlugin(ChatHandler.class);
        InheritedPluginsManager.loadInheritedPlugin(PermissionsManager.class);
        InheritedPluginsManager.loadInheritedPlugin(StaffPlugin.class);

        InheritedPluginsManager.onEnable();

        long diff = System.currentTimeMillis() - start;
        CUtils.inform("Core", "Core initiated in " + ((double) diff / 1000) + " seg.");
    }

    @Override
    public void onDisable() {
        serverManager.remove();
        InheritedPluginsManager.onDisable();
    }
}
