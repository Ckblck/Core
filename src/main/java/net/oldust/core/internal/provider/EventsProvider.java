package net.oldust.core.internal.provider;

import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.defaults.ImmutableWrappedPlayerDatabase;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.PluginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Un ListenerProvider es aquel
 * que se encarga de proveer a partes del Core
 * de un evento muy utilizado.
 * <p>
 * Por ejemplo, para disminuir el verbose de muchos eventos
 * OnJoin.
 */

@SuppressWarnings("unchecked")
public class EventsProvider implements Listener {
    private final Map<Class<? extends Event>, List<Operation>> operations = new ConcurrentHashMap<>();

    {
        operations.putIfAbsent(PlayerJoinEvent.class, new ArrayList<>());
        operations.putIfAbsent(PlayerQuitEvent.class, new ArrayList<>());
    }

    public EventsProvider() {
        CUtils.registerEvents(this);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent e) {
        handle(e);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent e) {
        handle(e);
    }

    @EventHandler
    public void onEnable(PluginEnableEvent e) {
        changeOperationStatus(e, true);
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        changeOperationStatus(e, false);
    }

    private void changeOperationStatus(PluginEvent event, boolean active) {
        Class<? extends Plugin> aClass = event.getPlugin().getClass();

        Collection<List<Operation>> operationsList = operations.values();

        for (List<Operation> operationList : operationsList) {
            operationList
                    .stream()
                    .filter(operation -> operation.getPluginClass().equals(aClass))
                    .forEach(operation -> operation.setActive(active));
        }
    }

    private void handle(PlayerEvent e, Player player) {
        Class<? extends Event> clazz = e.getClass();
        WrappedPlayerDatabase database = PlayerManager.getInstance().get(player);

        if (database == null) { // Si esto pasa, estamos en la B, significaría la Wrapped no se pudo cachear lo suficientemente rápido.
            if (e instanceof PlayerJoinEvent) {
                CUtils.inform("Server", "A database disappeared! These aren't good news... Is the server working correctly?");
            }

            player.kickPlayer(Lang.DB_DISAPPEARED);

            return;
        }

        ImmutableWrappedPlayerDatabase immutableDb = new ImmutableWrappedPlayerDatabase(database);

        for (Operation<PlayerEvent> operation : operations.get(clazz)) {
            if (!operation.isActive()) continue;

            try {
                operation.accept(e, immutableDb);
            } catch (Exception ex) {
                ex.printStackTrace(); // Let the loop continue
            }
        }

    }

    private void handle(PlayerEvent e) {
        handle(e, e.getPlayer());
    }

    /**
     * Inserts a new operation at a given event,
     * with a specific priority.
     * <p>
     * NOTE: Higher priorities WILL BE EXECUTED FIRST.
     *
     * @param pluginClass class of the plugin firing this operation
     * @param eventClass  event to listen
     * @param operation   operation to execute when event gets fired
     * @param priority    priority of this operation
     * @param <F>         type of event, same class as {@param event}
     */

    public <F extends PlayerEvent> void newOperation(
            Class<? extends JavaPlugin> pluginClass,
            Class<? extends PlayerEvent> eventClass,
            BiConsumer<F, ImmutableWrappedPlayerDatabase> operation,
            EventPriority priority
    ) {

        Operation<F> operationInstance = new Operation<>(operation, priority, pluginClass);
        List<Operation> operationList = this.operations.get(eventClass);

        operationList.add(operationInstance);

        operationList.sort((o1, o2) -> {
            int otherSlot = o1.getPriority().getSlot();
            int currentSlot = o2.getPriority().getSlot();

            return Integer.compare(currentSlot, otherSlot);
        });

    }

    /**
     * Inserts a new operation at a given event,
     * with the priority {@link EventPriority#NORMAL}.
     * <p>
     * NOTE: Higher priorities WILL BE EXECUTED FIRST.
     *
     * @param pluginClass class of the plugin firing this operation
     * @param eventClass  class of the event to listen
     * @param operation   operation to execute when event gets fired
     * @param <F>         type of event, same class as {@param event}
     */

    public <F extends PlayerEvent> void newOperation(
            Class<? extends JavaPlugin> pluginClass,
            Class<? extends PlayerEvent> eventClass,
            BiConsumer<F, ImmutableWrappedPlayerDatabase> operation
    ) {
        newOperation(pluginClass, eventClass, operation, EventPriority.NORMAL);
    }

}
