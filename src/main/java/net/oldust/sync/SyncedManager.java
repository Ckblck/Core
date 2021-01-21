package net.oldust.sync;

import net.oldust.sync.wrappers.Savable;

public interface SyncedManager<T, U extends Savable<?>> {
    U get(T object);

    boolean contains(T object);

    void update(U object);

}
