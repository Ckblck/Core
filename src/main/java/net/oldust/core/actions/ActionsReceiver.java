package net.oldust.core.actions;

import net.oldust.sync.JedisManager;
import net.oldust.sync.serializer.Base64Serializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class ActionsReceiver extends JedisPubSub implements Runnable {
    public static final String PREFIX = "oldust_action";

    public ActionsReceiver() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try (Jedis jedis = JedisManager.getInstance().getPool().getResource()) {
            jedis.subscribe(this, PREFIX);
        }
    }

    @Override
    public void onMessage(String channel, String message) {
        boolean isAction = channel.startsWith(PREFIX);
        if (!isAction) return;

        Action<?> deserialize = Base64Serializer.deserialize(message);
        deserialize.execute();
    }

}
