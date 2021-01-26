package net.oldust.core.internal.provider;

import lombok.Getter;
import net.oldust.sync.wrappers.defaults.ImmutableWrappedPlayerDatabase;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEvent;

import java.util.function.BiConsumer;

/**
 * Una operación es aquella que es ejecutada
 * en el momento de que un evento es llamado.
 *
 * @param <T> evento al cual escuchar
 */

public class Operation<T extends PlayerEvent> {
    private final BiConsumer<T, ImmutableWrappedPlayerDatabase> biConsumer;
    @Getter
    private final EventPriority priority;

    public Operation(BiConsumer<T, ImmutableWrappedPlayerDatabase> biConsumer, EventPriority priority) {
        this.biConsumer = biConsumer;
        this.priority = priority;
    }

    /**
     * Creates a new operation with the
     * {@link EventPriority#NORMAL} priority.
     */

    public Operation(BiConsumer<T, ImmutableWrappedPlayerDatabase> biConsumer) {
        this.biConsumer = biConsumer;
        this.priority = EventPriority.NORMAL;
    }

    public void accept(T event, ImmutableWrappedPlayerDatabase database) {
        biConsumer.accept(event, database);
    }

}
