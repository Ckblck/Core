package net.oldust.sync.wrappers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ThreadSafe
public abstract class Savable<T> implements Serializable {
    private static final long serialVersionUID = 4353344353363L;

    private final Map<T, WrappedValue> data = new ConcurrentHashMap<>();

    public abstract String getId();

    public synchronized void putIfAbsent(T key, Serializable value) {
        data.putIfAbsent(key, new WrappedValue(value));
    }

    public synchronized boolean contains(T key) {
        return data.containsKey(key);
    }

    public synchronized void put(T key, Serializable value) {
        data.put(key, new WrappedValue(value));
    }

    public synchronized Optional<WrappedValue> getValueOptional(T key) {
        return Optional.ofNullable(getValue(key));
    }

    public synchronized WrappedValue getValue(T key) {
        return data.get(key);
    }

    public synchronized WrappedValue remove(T key) {
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
            if (value == null) {
                return null;
            }

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
