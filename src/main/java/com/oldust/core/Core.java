package com.oldust.core;

import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.chat.ChatHandler;
import com.oldust.core.commons.CommonsPlugin;
import com.oldust.core.commons.internal.EventsProvider;
import com.oldust.core.inherited.plugins.InheritedPluginsManager;
import com.oldust.core.interactive.panels.InteractivePanelManager;
import com.oldust.core.models.ModelPlugin;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.pool.ThreadPool;
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
        CUtils.inform("CORE", "Initializing core...");

        new ThreadPool();
        new JedisManager();
        new MySQLManager().validateAddress();

        eventsProvider = new EventsProvider();

        new PlayerManager();
        new ActionsReceiver();
        new PermissionsManager();
        // new Interceptor(); <- Packet Interceptor -> (DEBUG Only)

        serverManager = new ServerManager();
        inventoryManager = new InventoryManager(this);
        inventoryManager.init();

        new InteractivePanelManager();

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        InheritedPluginsManager.loadInheritedPlugin(ModelPlugin.class);
        InheritedPluginsManager.loadInheritedPlugin(ChatHandler.class);
        InheritedPluginsManager.loadInheritedPlugin(PermissionsManager.class);
        InheritedPluginsManager.loadInheritedPlugin(StaffPlugin.class);
        InheritedPluginsManager.loadInheritedPlugin(CommonsPlugin.class);

        InheritedPluginsManager.onEnable();

        long diff = System.currentTimeMillis() - start;
        CUtils.inform("CORE", "Initiated in " + ((double) diff / 1000) + " seg.");
    }

    @Override
    public void onDisable() {
        serverManager.remove();
        InheritedPluginsManager.onDisable();
    }
}
