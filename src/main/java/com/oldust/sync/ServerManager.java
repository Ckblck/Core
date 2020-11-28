package com.oldust.sync;

import com.oldust.core.Core;
import com.oldust.core.commons.EventsProvider;
import com.oldust.core.commons.Operation;
import com.oldust.core.utils.CUtils;
import com.oldust.sync.jedis.RedisRepository;
import com.oldust.sync.wrappers.defaults.OldustServer;
import lombok.Getter;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Manager que contiene a todos los servidores
 * conectados a través del Core. Obviamente, si un {@link OldustServer}
 * sufre un cambio, simplemente será un estado (no tiene impacto, supongamos
 * que ponemos MUTE en true, el servidor indicado no será muteado por obra y gracia), en cambio...
 * bueno, en cambio vas a bugear un poquito algunas cosas. A lo que voy, si vas a actualizar esto
 * acordate de generar el cambio en el servidor en cuestión.
 */

public class ServerManager {
    private static final String SERVER_LIST_NAME = "servers";

    @Getter
    private final OldustServer currentServer;
    private final RedisRepository<OldustServer> serverRepository;

    public ServerManager() {
        JedisPool pool = JedisManager.getInstance().getPool();
        serverRepository = new RedisRepository<>(pool, "sv_repo");

        currentServer = new OldustServer();
        serverRepository.put(currentServer);
        serverRepository.pushToList(SERVER_LIST_NAME, currentServer);

        EventsProvider provider = Core.getInstance().getEventsProvider();
        List<String> playersConnected = currentServer.getPlayersConnected();

        provider.newOperation(PlayerJoinEvent.class, new Operation<PlayerJoinEvent>((pl, db)
                -> {
            playersConnected.add(pl.getPlayer().getName());
            updateCurrent();
        }));

        provider.newOperation(PlayerQuitEvent.class, new Operation<PlayerQuitEvent>((pl, db)
                -> {
            playersConnected.remove(pl.getPlayer().getName());
            updateCurrent();
        }));
    }

    public boolean contains(String serverName) {
        return serverRepository.exists(serverName);
    }

    public void remove() {
        serverRepository.remove(currentServer.getId());
        serverRepository.removeFromList(SERVER_LIST_NAME, currentServer);
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

    public boolean isPlayerOnline(String playerName) {
        return getPlayerServer(playerName).isPresent();
    }

    public Optional<String> getPlayerServer(String player) {
        CUtils.warnSyncCall();

        List<String> servers = fetchServers();
        Set<OldustServer> svs = serverRepository.fetchElements(servers);

        return svs.stream()
                .filter(sv -> sv.getPlayersConnected().contains(player))
                .map(OldustServer::getServerName)
                .findAny();
    }

    public List<String> fetchServers() {
        return serverRepository.fetchList(SERVER_LIST_NAME);
    }

}
