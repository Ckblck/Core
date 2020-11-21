package com.oldust.sync.wrappers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.CheckForNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class WrappedPlayerDatabase implements Savable, Serializable {
    private static final long serialVersionUID = 2950836897235238L;

    private final Map<String, WrappedValue> data = new ConcurrentHashMap<>();
    private final UUID playerUUID;

    @Override
    public String getId() {
        return playerUUID.toString();
    }

    public void putIfAbsent(PlayerDatabaseKeys key, Serializable value) {
        data.putIfAbsent(key.getKey(), new WrappedValue(value));
    }

    public boolean contains(PlayerDatabaseKeys key) {
        return data.containsKey(key.getKey());
    }

    public void put(PlayerDatabaseKeys key, Serializable value) {
        data.put(key.getKey(), new WrappedValue(value));
    }

    public Optional<WrappedValue> getValueOptional(PlayerDatabaseKeys key) {
        return Optional.ofNullable(getValue(key));
    }

    @CheckForNull
    public WrappedValue getValue(PlayerDatabaseKeys key) {
        return data.get(key.getKey());
    }

    public WrappedValue remove(PlayerDatabaseKeys key) {
        return data.remove(key.getKey());
    }

    @RequiredArgsConstructor
    public static class WrappedValue implements Serializable {
        private static final long serialVersionUID = 453453248953457L;
        @Getter
        private final Object value;

        public boolean asBoolean() {
            return (boolean) value;
        }

        public int asInt() {
            return (int) value;
        }

        public long asLong() {
            return (long) value;
        }

        public short asShort() {
            return (short) value;
        }

        public String asString() {
            return (String) value;
        }

        public <T> T asClass(Class<T> clazz) {
            return clazz.cast(value);
        }

        @SuppressWarnings("unchecked")
        public <K, V> Map<K, V> asMap(Class<K> key, Class<V> val) {
            return (Map<K, V>) value;
        }

    }

}
