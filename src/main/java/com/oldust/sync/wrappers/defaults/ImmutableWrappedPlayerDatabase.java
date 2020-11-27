package com.oldust.sync.wrappers.defaults;

import com.oldust.sync.wrappers.PlayerDatabaseKeys;

import java.io.Serializable;

public class ImmutableWrappedPlayerDatabase extends WrappedPlayerDatabase {

    public ImmutableWrappedPlayerDatabase(WrappedPlayerDatabase database) {
        super(database.getPlayerUUID());
    }

    @Override
    public void put(PlayerDatabaseKeys key, Serializable value) {
        throw new UnsupportedOperationException("Cannot modify an ImmutableWrappedPlayerDatabase.");
    }

    @Override
    public void putIfAbsent(PlayerDatabaseKeys key, Serializable value) {
        throw new UnsupportedOperationException("Cannot modify an ImmutableWrappedPlayerDatabase.");
    }

    @Override
    public WrappedValue remove(PlayerDatabaseKeys key) {
        throw new UnsupportedOperationException("Cannot modify an ImmutableWrappedPlayerDatabase.");
    }

}
