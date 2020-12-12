package com.oldust.core;

import com.oldust.core.actions.ActionsReceiver;
import com.oldust.core.chat.ChatHandler;
import com.oldust.core.commons.CommonsPlugin;
import com.oldust.core.commons.internal.EventsProvider;
import com.oldust.core.commons.permission.PermissionsManager;
import com.oldust.core.inherited.plugins.InheritedPluginsManager;
import com.oldust.core.interactive.panels.InteractivePanelManager;
import com.oldust.core.models.ModelPlugin;
import com.oldust.core.mysql.MySQLManager;
import com.oldust.core.pool.ThreadPool;
import com.oldust.core.staff.StaffPlugin;
import com.oldust.core.utils.CUtils;
import com.oldust.sync.JedisManager;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.ServerManager;
import fr.minuskube.inv.InventoryManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Clase inicial del Core.
 * <p>
 * El Core adapta en grandes medidas el uso del patrón Singleton.
 * Sin embargo, también hay situaciones en donde se prefiere
 * la inyección de dependencias.
 * <p>
 * Toda query a Redis debe hacerse asíncronamente, para evitar
 * bloquear el main thread. Si esto no se cumple, un StackTrace será
 * mostrado en la consola indicando qué operación específica se ejecutó en el Main thread.
 * <p>
 * El Core no posee algo tal como 'canales de chat', en cambio, se puede hacer
 * uso de {@link com.oldust.core.actions.types.DispatchMessageAction} con un {@link com.oldust.core.utils.lambda.SerializablePredicate}
 * ofreciendo así un gran grado de flexibilidad.
 * <p>
 * Para mostrar inventarios se utiliza 'SmartInvs'. Ejemplos de uso:
 * {@link com.oldust.core.staff.logs.LogsInventory}, {@link com.oldust.core.staff.playerdata.PlayerDataInventory}
 * <p>
 * La clase {@link MySQLManager} ha sido pensada para el uso entre plugins.
 * Es por ello que la mayoría de sus métodos son estáticos (aunque también podrían no serlos).
 * Para poder utilizarla, primero se debe instanciar {@link ThreadPool} y luego {@link MySQLManager}.
 * La utilización de Threads propios nos permite ofrecer
 * compatibilidad tanto para Bukkit como para BungeeCord.
 * <p>
 * Cuando {@link #onEnable()} se ejecute, una nueva instancia
 * de {@link com.oldust.sync.wrappers.defaults.OldustServer} se creará
 * y registrará en Redis.
 * <p>
 * Para evitar el registro desmedido de {@link org.bukkit.event.player.PlayerLoginEvent} y {@link org.bukkit.event.player.PlayerQuitEvent},
 * se ha creado una clase para unificar ello, {@link EventsProvider}.
 */

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
        CUtils.inform("Core", "Initiated in " + ((double) diff / 1000) + " seg.");
    }

    @Override
    public void onDisable() {
        serverManager.remove();

        InheritedPluginsManager.onDisable();
    }

}
