package net.oldust.core.actions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.oldust.sync.serializer.Base64Serializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.Serializable;

@RequiredArgsConstructor
public abstract class Action<T extends Action<?>> implements Serializable {
    @Getter
    private final String channel;

    protected abstract void execute();

    public void push(JedisPool pool) {
        String serialized = Base64Serializer.serialize(this);

        try (Jedis jedis = pool.getResource()) {
            jedis.publish(channel, serialized);
        }

    }

}
