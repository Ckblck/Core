package com.oldust.sync.jedis;

import com.oldust.sync.serializer.Base64Serializer;
import com.oldust.sync.wrappers.Savable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.SetParams;

import java.util.*;

public class RedisRepository<T extends Savable<?>> {
    private final JedisPool pool;
    private final String repositoryName;

    public RedisRepository(JedisPool pool, String repositoryName) {
        this.pool = pool;
        this.repositoryName = repositoryName;
    }

    public void pushToList(String list, T element) {
        try (Jedis jedis = pool.getResource()) {
            String keyName = getKeyName(list);

            jedis.lpush(keyName, element.getId());
        }
    }

    public void removeFromList(String list, T element) {
        try (Jedis jedis = pool.getResource()) {
            String keyName = getKeyName(list);

            jedis.lrem(keyName, 0, element.getId());
        }
    }

    public List<String> fetchList(String list) {
        try (Jedis jedis = pool.getResource()) {
            String keyName = getKeyName(list);

            return jedis.lrange(keyName, 0, -1);
        }
    }

    /**
     * Obtiene una lista de elementos
     * a partir de ciertas keys.
     *
     * @param keys keys simbolizando el nombre del servidor
     *             ej: "lobby", el cual se convierte en el método a
     *             repository_name.lobby
     * @return lista de elementos del tipo
     */

    public Set<T> fetchElements(Collection<String> keys) {
        Set<T> elements = new HashSet<>();

        try (Jedis jedis = pool.getResource()) {
            Pipeline pipeline = jedis.pipelined();
            List<Response<String>> responses = new ArrayList<>();

            for (String key : keys) {
                responses.add(pipeline.get(getKeyName(key)));
            }

            pipeline.sync();

            for (Response<String> response : responses) {
                String serializedData = response.get();
                T element = Base64Serializer.deserialize(serializedData);

                elements.add(element);
            }

        }

        return elements;
    }

    public void put(T element) {
        try (Jedis jedis = pool.getResource()) {
            String key = getKeyName(element);
            String serialize = Base64Serializer.serialize(element);

            jedis.set(key, serialize);
        }
    }

    public T get(String key) {
        try (Jedis jedis = pool.getResource()) {
            String keyName = getKeyName(key);
            String serialize = jedis.get(keyName);

            if (serialize == null) return null;

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
            String serializedData = Base64Serializer.serialize(element);
            String key = getKeyName(element);

            jedis.set(key, serializedData, SetParams.setParams().xx());
        }
    }

    public boolean exists(String name) {
        try (Jedis jedis = pool.getResource()) {
            return jedis.exists(getKeyName(name));
        }
    }

    private String getKeyName(T element) {
        return getKeyName(element.getId());
    }

    private String getKeyName(String id) {
        return repositoryName + "." + id;
    }

}