package net.oldust.sync.wrappers.defaults;

import lombok.RequiredArgsConstructor;
import net.oldust.sync.wrappers.PlayerDatabaseKeys;
import net.oldust.sync.wrappers.Savable;

import java.util.Optional;

@RequiredArgsConstructor
public class ImmutableWrappedPlayerDatabase {
    private final WrappedPlayerDatabase database;

    public boolean contains(PlayerDatabaseKeys key) {
        return database.contains(key);
    }

    public Optional<Savable.WrappedValue> getValueOptional(PlayerDatabaseKeys key) {
        return database.getValueOptional(key);
    }

    public Savable.WrappedValue getValue(PlayerDatabaseKeys key) {
        return database.getValue(key);
    }

    public String getId() {
        return database.getId();
    }

}
