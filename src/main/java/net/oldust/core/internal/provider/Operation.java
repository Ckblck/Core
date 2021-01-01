package net.oldust.core.internal.provider;

import net.oldust.sync.wrappers.defaults.ImmutableWrappedPlayerDatabase;
import org.bukkit.event.player.PlayerEvent;

/**
 * Una operación es aquella que es ejecutada
 * en el momento de que un evento es llamado.
 *
 * @param <T> evento al cual escuchar
 */

@FunctionalInterface
public interface Operation<T extends PlayerEvent> {
    void run(T event, ImmutableWrappedPlayerDatabase database);
}
