package net.oldust.core.commons.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.oldust.sync.wrappers.defaults.ImmutableWrappedPlayerDatabase;
import org.bukkit.event.Event;

import java.util.function.BiConsumer;

/**
 * Una operación es aquella que es ejecutada
 * en el momento de que un evento es llamado.
 *
 * @param <T> evento al cual escuchar
 */

@Getter
@RequiredArgsConstructor
public class Operation<T extends Event> {
    private final BiConsumer<T, ImmutableWrappedPlayerDatabase> consumer;
}