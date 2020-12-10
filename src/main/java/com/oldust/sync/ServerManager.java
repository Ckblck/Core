package com.oldust.sync;

import com.oldust.core.Core;
import com.oldust.core.commons.internal.EventsProvider;
import com.oldust.core.commons.internal.Operation;
import com.oldust.core.utils.CUtils;
import com.oldust.sync.jedis.RedisRepository;
import com.oldust.sync.wrappers.defaults.OldustServer;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.JedisPool;

import java.util.*;

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

        CUtils.runAsync(() -> {
            serverRepository.put(currentServer);
            serverRepository.pushToList(SERVER_LIST_NAME, currentServer);
        });

        EventsProvider provider = Core.getInstance().getEventsProvider();

        provider.newOperation(PlayerJoinEvent.class, new Operation<PlayerJoinEvent>((pl, db)
                -> {
            Map<String, UUID> playersConnected = currentServer.getPlayersConnected();
            Player player = pl.getPlayer();

            playersConnected.put(player.getName(), player.getUniqueId());

            CUtils.runAsync(this::updateCurrent);
        }));

        provider.newOperation(PlayerQuitEvent.class, new Operation<PlayerQuitEvent>((pl, db)
                -> {
            Map<String, UUID> playersConnected = currentServer.getPlayersConnected();
            Player player = pl.getPlayer();

            playersConnected.remove(player.getName());

            CUtils.runAsync(this::updateCurrent);
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
        List<String> servers = fetchServers();
        Set<OldustServer> svs = serverRepository.fetchElements(servers);

        return svs.stream()
                .filter(sv -> sv.getPlayersConnected().keySet()
                        .stream()
                        .anyMatch(pl -> pl.equalsIgnoreCase(player)))
                .map(OldustServer::getServerName)
                .findAny();
    }

    public List<String> fetchServers() {
        return serverRepository.fetchList(SERVER_LIST_NAME);
    }

}
