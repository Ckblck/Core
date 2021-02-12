package net.oldust.core.actions;

import net.oldust.core.actions.reliable.ProxyReliableAction;
import net.oldust.core.utils.CUtils;
import net.oldust.sync.jedis.JedisManager;
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
        Action<?> deserialize = Base64Serializer.deserialize(message);

        boolean notProxyAction = CUtils.IS_PROXY && (!(deserialize instanceof ProxyReliableAction));

        if (notProxyAction)
            return;

        boolean notServerAction = !CUtils.IS_PROXY && deserialize instanceof ProxyReliableAction;

        if (notServerAction)
            return;

        if (deserialize == null) { // Should not happen.
            CUtils.inform("Actions", "Detected null action after deserialization for channel: " + channel);

            return;
        }

        deserialize.execute();
    }

}
