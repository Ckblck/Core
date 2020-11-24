package com.oldust.sync;

import com.oldust.sync.jedis.RedisRepository;
import com.oldust.sync.wrappers.defaults.OldustServer;
import lombok.Getter;
import redis.clients.jedis.JedisPool;

/**
 * Manager que contiene a todos los servidores
 * conectados a través del Core. Obviamente, si un {@link OldustServer}
 * sufre un cambio, simplemente será un estado (no tiene impacto, supongamos
 * que ponemos MUTE en true, el servidor indicado no será muteado por obra y gracia), en cambio...
 * bueno, en cambio vas a bugear un poquito algunas cosas. A lo que voy, si vas a actualizar esto
 * acordate de generar el cambio en el servidor en cuestión.
 */

public class ServerManager {
    @Getter
    private final OldustServer currentServer;
    private final RedisRepository<OldustServer> serverRepository;

    public ServerManager() {
        JedisPool pool = JedisManager.getInstance().getPool();
        serverRepository = new RedisRepository<>(pool, "sv_repo");

        currentServer = new OldustServer();
        serverRepository.put(currentServer);
    }

    public boolean contains(String serverName) {
        return serverRepository.exists(serverName);
    }

    public void remove() {
        serverRepository.remove(currentServer.getId());
    }

    public OldustServer getServer(String serverName) {
        return serverRepository.get(serverName);
    }

    public void updateCurrent() {
        serverRepository.update(currentServer);
    }

    public void update(OldustServer server) {
        serverRepository.update(server);
    }

}
