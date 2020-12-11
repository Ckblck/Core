package com.oldust.core.commons.internal;

import com.oldust.core.utils.CUtils;
import com.oldust.core.utils.lang.Lang;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.defaults.ImmutableWrappedPlayerDatabase;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
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
 * Además, ayuda a reducir la carga de Redis debido
 * a que solo obtiene la database una única vez por evento.
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

    private void handle(Event e, Player player) {
        CUtils.runAsync(() -> {
            Class<? extends Event> clazz = e.getClass();
            WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());

            if (database == null) {
                player.kickPlayer(Lang.DB_DISAPPEARED);

                return;
            }

            CUtils.runSync(() -> {
                for (Operation<Event> operation : operations.get(clazz)) {
                    operation.getConsumer().accept(e, new ImmutableWrappedPlayerDatabase(database));
                }
            });

        });
    }

    private void handle(PlayerEvent e) {
        handle(e, e.getPlayer());
    }

    public void newOperation(Class<? extends Event> event, Operation<? extends Event> operation) {
        operations.get(event).add(operation);
    }

}
