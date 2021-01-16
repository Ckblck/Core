package net.oldust.sync;

import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.internal.provider.EventsProvider;
import net.oldust.core.utils.Async;
import net.oldust.core.utils.CUtils;
import net.oldust.sync.jedis.RedisRepository;
import net.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {
    @Getter
    private static PlayerManager instance;

    private final RedisRepository<WrappedPlayerDatabase> playerRepository;

    private final Map<UUID, WrappedPlayerDatabase> cache = new ConcurrentHashMap<>();

    public PlayerManager() {
        instance = this;

        JedisPool pool = JedisManager.getInstance().getPool();
        playerRepository = new RedisRepository<>(pool, "pl_repo");

        Core core = Core.getInstance();

        if (core != null) { // Is null for the OldustBungee
            EventsProvider provider = core.getEventsProvider();

            provider.newOperation(PlayerQuitEvent.class, (ev, db) -> {
                UUID uuid = ev.getPlayer().getUniqueId();

                cache.remove(uuid);
            });
        }

    }

    public void cacheDatabase(WrappedPlayerDatabase database) {
        UUID uuid = database.getPlayerUUID();

        cache.put(uuid, database);
    }

    public void saveDatabase(WrappedPlayerDatabase database) { // Executed in OldustBungee
        playerRepository.put(database);
    }

    public WrappedPlayerDatabase getDatabase(UUID uuid) {
        return cache.get(uuid);
    }

    public WrappedPlayerDatabase getDatabase(Player player) {
        return cache.get(player.getUniqueId());
    }

    @Async
    public WrappedPlayerDatabase getDatabaseRedis(UUID uuid) {
        return playerRepository.get(uuid.toString());
    }

    @Async
    public WrappedPlayerDatabase getDatabaseRedis(Player player) {
        return getDatabaseRedis(player.getUniqueId());
    }

    public void remove(UUID uuid) { // Executed in OldustBungee
        cache.remove(uuid);

        CUtils.runAsync(() ->
                playerRepository.remove(uuid.toString()));
    }

    public void update(WrappedPlayerDatabase database) {
        UUID uuid = database.getPlayerUUID();

        cache.replace(uuid, database);

        CUtils.runAsync(() ->
                playerRepository.update(database));
    }

    public boolean contains(UUID uuid) {
        return cache.containsKey(uuid);
    }

    @Async
    public boolean containsRedis(UUID uuid) {
        return playerRepository.exists(uuid.toString());
    }

}
