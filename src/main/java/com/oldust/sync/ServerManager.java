package com.oldust.sync;

import com.oldust.sync.jedis.RedisRepository;
import com.oldust.sync.wrappers.defaults.OldustServer;
import lombok.Getter;
import redis.clients.jedis.JedisPool;

public class ServerManager {
    @Getter
    private static ServerManager instance;

    private final OldustServer currentServer;
    private final RedisRepository<OldustServer> serverRepository;

    public ServerManager() {
        instance = this;

        JedisPool pool = JedisManager.getInstance().getPool();
        serverRepository = new RedisRepository<>(pool, "sv_repo");

        currentServer = new OldustServer();
        serverRepository.add(currentServer);
    }

    public boolean contains(String serverName) {
        return serverRepository.exists(serverName);
    }

    public void remove() {
        serverRepository.remove(currentServer.getId());
    }

}
