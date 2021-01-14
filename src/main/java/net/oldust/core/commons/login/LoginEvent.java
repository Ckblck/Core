package net.oldust.core.commons.login;

import net.oldust.core.Core;
import net.oldust.core.utils.CUtils;
import net.oldust.core.utils.lang.Lang;
import net.oldust.sync.PlayerManager;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Force the server to
 * cache the database from Redis.
 */

public class LoginEvent implements Listener {
    private static final String KICK_MESSAGE = Lang.ERROR_COLOR + "Could not download your database :(" +
            "\n" + Lang.ERROR_COLOR + "Try joining again.";

    public LoginEvent() {
        CUtils.registerEvents(this);
    }

    /**
     * This event gets fired AFTER
     * OldustBungee's {@link net.md_5.bungee.api.event.ServerConnectEvent} event.
     * That means that OldustBungee updates the database with this {@link Core#getServerName()},
     * and then we cache that updated database.
     */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(AsyncPlayerPreLoginEvent e) {
        AsyncPlayerPreLoginEvent.Result result = e.getLoginResult();

        if (result != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        PlayerManager playerManager = PlayerManager.getInstance();
        WrappedPlayerDatabase databaseRedis = playerManager.getDatabaseRedis(e.getUniqueId());

        databaseRedis.setBungeeServer(Core.getInstance().getServerName());

        boolean success = playerManager.cacheDatabase(databaseRedis);

        if (!success) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, KICK_MESSAGE); // Happens almost never (might occur during the server initialization).
        } else {
            playerManager.update(databaseRedis);
        }

    }

}
