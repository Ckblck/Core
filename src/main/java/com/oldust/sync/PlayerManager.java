package com.oldust.sync;

import com.oldust.sync.jedis.RedisRepository;
import com.oldust.sync.wrappers.defaults.WrappedPlayerDatabase;
import lombok.Getter;
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

    public void saveDatabase(WrappedPlayerDatabase database) {
        playerRepository.put(database);
    }

    public WrappedPlayerDatabase getDatabase(UUID uuid) {
        return playerRepository.get(uuid.toString());
    }

    public void remove(UUID uuid) { // Ejecutado en OldustBungee
        playerRepository.remove(uuid.toString());
    }

    public void update(WrappedPlayerDatabase database) {
        playerRepository.update(database);
    }

}
