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

public class PlayerManager implements SyncedManager<UUID, WrappedPlayerDatabase> {
    @Getter
    private static PlayerManager instance;

    private final RedisRepository<WrappedPlayerDatabase> playerRepository;

    private final Map<UUID, WrappedPlayerDatabase> cache = new ConcurrentHashMap<>();

    public PlayerManager() {
        instance = this;

        JedisPool pool = JedisManager.getInstance().getPool();
        playerRepository = new RedisRepository<>(pool, "pl_repo");

        Core core = Core.getInstance();

        if (core != null) { // Is null when the instantiation is done as a Bungee Plugin
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

    @Override
    public WrappedPlayerDatabase get(UUID playerUuid) {
        return cache.get(playerUuid);
    }

    @Override
    public boolean contains(UUID uuid) {
        return cache.containsKey(uuid);
    }

    @Override
    public void update(WrappedPlayerDatabase database) {
        CUtils.runAsync(() ->
                playerRepository.update(database));
    }

    public WrappedPlayerDatabase get(Player player) {
        return get(player.getUniqueId());
    }

    @Async
    public WrappedPlayerDatabase getDatabaseRedis(UUID uuid) {
        return playerRepository.get(uuid.toString());
    }

    @Async
    public WrappedPlayerDatabase getDatabaseRedis(Player player) {
        return getDatabaseRedis(player.getUniqueId());
    }

    public void remove(UUID uuid) { // Invoked in OldustBungee
        cache.remove(uuid);

        CUtils.runAsync(() ->
                playerRepository.remove(uuid.toString()));
    }

    @Async
    public boolean containsRedis(UUID uuid) {
        return playerRepository.exists(uuid.toString());
    }

}
