package com.oldust.sync.jedis;

import com.oldust.sync.serializer.Base64Serializer;
import com.oldust.sync.wrappers.Savable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.io.Serializable;

public class RedisRepository<T extends Savable> {
    private final JedisPool pool;
    private final String repositoryName;

    public RedisRepository(JedisPool pool, String repositoryName) {
        this.pool = pool;
        this.repositoryName = repositoryName;
    }

    public void add(T element) {
        try (Jedis jedis = pool.getResource()) {
            String key = getKeyName(element);
            String serialize = Base64Serializer.serialize((Serializable) element);

            jedis.set(key, serialize);
        }
    }

    public T get(String key) {
        try (Jedis jedis = pool.getResource()) {
            String keyName = getKeyName(key);
            String serialize = jedis.get(keyName);

            return Base64Serializer.deserialize(serialize);
        }
    }

    public void remove(String key) {
        try (Jedis jedis = pool.getResource()) {
            String keyName = getKeyName(key);

            jedis.del(keyName);
        }
    }

    public void update(T element) {
        try (Jedis jedis = pool.getResource()) {
            String serializedData = Base64Serializer.serialize((Serializable) element);
            String key = getKeyName(element);

            jedis.set(key, serializedData, SetParams.setParams().xx());
        }
    }

    private String getKeyName(T element) {
        return getKeyName(element.getId());
    }

    private String getKeyName(String id) {
        return repositoryName + "." + id;
    }

}