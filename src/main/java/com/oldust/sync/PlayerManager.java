package com.oldust.sync;

import com.oldust.core.utils.lang.Async;
import com.oldust.sync.jedis.RedisRepository;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import lombok.Getter;
import org.bukkit.entity.Player;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

public class PlayerManager {
    @Getter
    private static PlayerManager instance;

    private final RedisRepository<WrappedPlayerDatabase> playerRepository;

    public PlayerManager() {
        instance = this;

        JedisPool pool = JedisManager.getInstance().getPool();
        playerRepository = new RedisRepository<>(pool, "pl_repo");
    }

    @Async
    public void saveDatabase(WrappedPlayerDatabase database) {
        playerRepository.put(database);
    }

    @Async
    public WrappedPlayerDatabase getDatabase(UUID uuid) {
        return playerRepository.get(uuid.toString());
    }

    @Async
    public WrappedPlayerDatabase getDatabase(Player player) {
        return getDatabase(player.getUniqueId());
    }

    @Async
    public void remove(UUID uuid) { // Ejecutado en OldustBungee
        playerRepository.remove(uuid.toString());
    }

    @Async
    public void update(WrappedPlayerDatabase database) {
        playerRepository.update(database);
    }

    @Async
    public boolean contains(UUID uuid) {
        return playerRepository.exists(uuid.toString());
    }

}
