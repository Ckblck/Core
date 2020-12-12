package com.oldust.sync.wrappers.defaults;

import com.oldust.sync.wrappers.PlayerDatabaseKeys;
import com.oldust.sync.wrappers.Savable;
import lombok.RequiredArgsConstructor;

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
