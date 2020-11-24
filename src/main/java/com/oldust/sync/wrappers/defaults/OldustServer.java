package com.oldust.sync.wrappers.defaults;

import com.oldust.core.Core;
import com.oldust.sync.wrappers.Savable;
import com.oldust.sync.wrappers.ServerDatabaseKeys;

public class OldustServer extends Savable<ServerDatabaseKeys> {
    private static final long serialVersionUID = 645603945648576L;

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
