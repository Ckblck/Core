package net.oldust.sync;

import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.internal.provider.EventsProvider;
import net.oldust.core.utils.Async;
import net.oldust.core.utils.CUtils;
import net.oldust.sync.jedis.JedisManager;
import net.oldust.sync.jedis.RedisRepository;
import net.oldust.sync.wrappers.defaults.OldustServer;
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

public class ServerManager implements SyncedManager<String, OldustServer> {
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

        provider.newOperation(PlayerJoinEvent.class, (pl, db) -> {
            Map<String, UUID> playersConnected = currentServer.getPlayersConnected();
            Player player = pl.getPlayer();

            playersConnected.put(player.getName(), player.getUniqueId());

            CUtils.runAsync(this::updateCurrent);
        });

        provider.newOperation(PlayerQuitEvent.class, (pl, db) -> {
            Map<String, UUID> playersConnected = currentServer.getPlayersConnected();
            Player player = pl.getPlayer();

            playersConnected.remove(player.getName());

            CUtils.runAsync(this::updateCurrent);
        });

    }

    @Async
    @Override
    public OldustServer get(String serverName) {
        return serverRepository.get(serverName);
    }

    @Async
    @Override
    public boolean contains(String serverName) {
        return serverRepository.exists(serverName);
    }

    @Async
    public void remove() {
        serverRepository.remove(currentServer.getId());
        serverRepository.removeFromList(SERVER_LIST_NAME, currentServer);
    }

    @Async
    public void updateCurrent() {
        serverRepository.update(currentServer);
    }

    @Async
    @Override
    public void update(OldustServer server) {
        serverRepository.update(server);
    }

    @Async
    public boolean isPlayerOnline(String playerName) {
        return getPlayerServer(playerName).isPresent();
    }

    @Async
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

    @Async
    public List<String> fetchServers() {
        return serverRepository.fetchList(SERVER_LIST_NAME);
    }

}
