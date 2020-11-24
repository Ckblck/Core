package com.oldust.sync.wrappers.defaults;

import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.Savable;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class WrappedPlayerDatabase extends Savable<PlayerDatabaseKeys> {
    private static final long serialVersionUID = 2950836897235238L;

    private final UUID playerUUID;

    @Override
    public String getId() {
        return playerUUID.toString();
    }

}
