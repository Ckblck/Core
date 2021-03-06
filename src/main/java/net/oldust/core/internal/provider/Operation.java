package net.oldust.core.internal.provider;

import lombok.Getter;
import net.oldust.sync.wrappers.defaults.ImmutableWrappedPlayerDatabase;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
    @Getter
    private final Class<? extends JavaPlugin> pluginClass;

    public Operation(BiConsumer<T, ImmutableWrappedPlayerDatabase> biConsumer, EventPriority priority, Class<? extends JavaPlugin> pluginClass) {
        this.biConsumer = biConsumer;
        this.priority = priority;
        this.pluginClass = pluginClass;
    }

    public void accept(T event, ImmutableWrappedPlayerDatabase database) {
        biConsumer.accept(event, database);
    }

}
