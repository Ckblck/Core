package com.oldust.core.commons;

import com.oldust.core.utils.CUtils;
import com.oldust.sync.PlayerManager;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private final Map<Class<? extends Event>, Set<Operation>> operations = new HashMap<>();

    {
        operations.putIfAbsent(PlayerJoinEvent.class, new HashSet<>());
        operations.putIfAbsent(PlayerQuitEvent.class, new HashSet<>());
    }

    public EventsProvider() {
        CUtils.registerEvents(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        handle(e);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        handle(e);
    }

    private void handle(Event e, Player player) {
        Class<? extends Event> clazz = e.getClass();

        WrappedPlayerDatabase database = PlayerManager.getInstance().getDatabase(player.getUniqueId());

        for (Operation<Event> operation : operations.get(clazz)) {
            operation.getConsumer().accept(e, database);
        }
    }

    private void handle(PlayerEvent e) {
        handle(e, e.getPlayer());
    }

    public void newOperation(Class<? extends Event> event, Operation<? extends Event> operation) {
        operations.get(event).add(operation);
    }

}
