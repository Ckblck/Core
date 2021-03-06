package net.oldust.core.actionbar;

import net.oldust.core.Core;
import net.oldust.core.internal.provider.EventsProvider;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActionbarManager extends BukkitRunnable {
    private final Map<UUID, Actionbar> actionbars = new HashMap<>();

    public ActionbarManager() {
        Core core = Core.getInstance();
        EventsProvider provider = core.getEventsProvider();

        provider.newOperation(Core.class, PlayerQuitEvent.class, (ev, db)
                -> actionbars.remove(ev.getPlayer().getUniqueId()));

        runTaskTimerAsynchronously(core, 0, 1);
    }

    @Override
    public void run() {
        Collection<Actionbar> actionbars = this.actionbars.values();

        for (Actionbar bar : actionbars) {
            bar.update();
        }

    }

}
