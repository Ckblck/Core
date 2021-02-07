package net.oldust.core.actions.reliable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import net.oldust.core.Core;
import net.oldust.sync.JedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AckReceiver extends JedisPubSub implements Runnable {
    public static final String ACTION_ACKNOWLEDGE = "action_acknowledge";

    private final Cache<String, Consumer<Acknowledgment>> acknowledgementCache = CacheBuilder
            .newBuilder()
            .expireAfterWrite(3, TimeUnit.SECONDS)
            .removalListener((RemovalListener<String, Consumer<Acknowledgment>>) notification -> {
                boolean wasEvicted = notification.wasEvicted();

                if (wasEvicted) {
                    Consumer<Acknowledgment> consumer = notification.getValue();

                    consumer.accept(Acknowledgment.INVALID);
                }

            })
            .build();

    public AckReceiver() {
        new Thread(this).start();

        new Thread(() -> {
            while (Core.getInstance().isEnabled()) {
                acknowledgementCache.cleanUp();

                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onMessage(String channel, String message) {
        String identifier = ReliableAction.extractIdentifier(message);

        boolean forThisServer = contains(identifier); // If this is false, then this acknowledgment is not meant for this server.

        if (forThisServer) {
            Acknowledgment value = ReliableAction.extractAcknowledgement(message);

            Consumer<Acknowledgment> acknowledgment = remove(identifier);

            acknowledgment.accept(value);
        }

    }

    @Override
    public void run() {
        try (Jedis jedis = JedisManager.getInstance().getPool().getResource()) {
            jedis.subscribe(this, ACTION_ACKNOWLEDGE);
        }
    }

    private Consumer<Acknowledgment> remove(String identifier) {
        return acknowledgementCache.asMap()
                .remove(identifier);
    }

    private boolean contains(String identifier) {
        return acknowledgementCache.asMap()
                .containsKey(identifier);
    }

    public void registerReliableAction(ReliableAction action, Consumer<Acknowledgment> whenAcknowledged) {
        acknowledgementCache.put(action.getIdentifier(), whenAcknowledged);
    }

}
