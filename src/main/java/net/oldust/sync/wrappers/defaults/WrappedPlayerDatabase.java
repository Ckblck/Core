package net.oldust.sync.wrappers.defaults;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.Savable;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class WrappedPlayerDatabase extends Savable<PlayerDatabaseKeys> {
    private static final long serialVersionUID = 2950836897235238L;

    private final UUID playerUUID;
    @Setter
    private String bungeeServer;

    @Override
    public String getId() {
        return playerUUID.toString();
    }

}
