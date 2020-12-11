package com.oldust.sync.wrappers.defaults;

import com.oldust.core.Core;
import com.oldust.sync.wrappers.Savable;
import com.oldust.sync.wrappers.ServerDatabaseKeys;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Clase que simboliza un servidor
 * conectado a la red de Oldust.
 * Funciona de manera casi idéntica a un {@link WrappedPlayerDatabase}.
 * Cuenta con keys, ({@link ServerDatabaseKeys}) y un mapa
 * que contiene a todos los jugadores conectados en aquel servidor.
 */

@Getter
public class OldustServer extends Savable<ServerDatabaseKeys> {
    private static final long serialVersionUID = 645603945648576L;

    // Key = Nickname, Value = UUID
    private final Map<String, UUID> playersConnected = new HashMap<>();
    private final String serverName;

    public OldustServer() {
        this.serverName = Core.getInstance().getServerName();

        put(ServerDatabaseKeys.MUTED, false);
    }

    @Override
    public String getId() {
        return serverName;
    }

}
