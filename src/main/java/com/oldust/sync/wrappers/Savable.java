package com.oldust.sync.wrappers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Savable<T> implements Serializable {
    private final Map<T, WrappedValue> data = new ConcurrentHashMap<>();

    public abstract String getId();

    public void putIfAbsent(T key, Serializable value) {
        data.putIfAbsent(key, new WrappedValue(value));
    }

    public boolean contains(T key) {
        return data.containsKey(key);
    }

    public void put(T key, Serializable value) {
        data.put(key, new WrappedValue(value));
    }

    public Optional<WrappedValue> getValueOptional(T key) {
        return Optional.ofNullable(getValue(key));
    }

    public WrappedValue getValue(T key) {
        return data.get(key);
    }

    public WrappedValue remove(T key) {
        return data.remove(key);
    }

    @SuppressWarnings("unused")
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

        public <F> F asClass(Class<F> clazz) {
            return clazz.cast(value);
        }

        @SuppressWarnings("unchecked")
        public <K, V> Map<K, V> asMap(Class<K> key, Class<V> val) {
            return (Map<K, V>) value;
        }

        @SuppressWarnings("unchecked")
        public <V> Set<V> asSet(Class<V> key) {
            return (Set<V>) value;
        }

    }

}
