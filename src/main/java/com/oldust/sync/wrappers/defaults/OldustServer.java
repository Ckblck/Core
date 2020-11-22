package com.oldust.sync.wrappers.defaults;

import com.oldust.core.Core;
import com.oldust.sync.wrappers.Savable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OldustServer implements Savable, Serializable {
    private static final long serialVersionUID = 645603945648576L;

    private final List<UUID> playersConnected = new ArrayList<>();
    private final String serverName;

    public OldustServer() {
        this.serverName = Core.getInstance().getServerName();
    }

    @Override
    public String getId() {
        return serverName;
    }

}
