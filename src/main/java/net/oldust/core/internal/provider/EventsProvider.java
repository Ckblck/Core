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

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

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
    private final Map<Class<? extends Event>, Queue<Operation>> operations = new ConcurrentHashMap<>();

    {
        operations.putIfAbsent(PlayerJoinEvent.class, new ConcurrentLinkedQueue<>());
        operations.putIfAbsent(PlayerQuitEvent.class, new ConcurrentLinkedQueue<>());
    }

    public EventsProvider() {
        CUtils.registerEvents(this);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        handle(e);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e) {
        handle(e);
    }

    private void handle(PlayerEvent e, Player player) {
        Class<? extends Event> clazz = e.getClass();
        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player);

        if (database == null) { // Si esto pasa, estamos en la B, significaría que 'MandatoryCacheWrappedAction' no fue capaz de enviarse lo suficientemente rápido para cachear la Wrapped.
            CUtils.inform("Server", "A database disappeared! These aren't good news... Is redis working correctly?");
            player.kickPlayer(Lang.DB_DISAPPEARED);

            return;
        }

        ImmutableWrappedPlayerDatabase immutableDb = new ImmutableWrappedPlayerDatabase(database);

        for (Operation<PlayerEvent> operation : operations.get(clazz)) {
            operation.run(e, immutableDb);
        }

    }

    private void handle(PlayerEvent e) {
        handle(e, e.getPlayer());
    }

    public void newOperation(Class<? extends PlayerEvent> event, Operation<? extends Event> operation) {
        operations.get(event).add(operation);
    }

}
