package net.oldust.core.actions.reliable;

import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.actions.Action;
import net.oldust.core.actions.ActionsReceiver;
import net.oldust.sync.JedisManager;
import net.oldust.sync.serializer.Base64Serializer;
import org.apache.commons.lang.RandomStringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.function.Consumer;

/**
 * An action which acknowledges
 * the receive and proper execution.
 */

@Getter
public abstract class ReliableAction extends Action<ReliableAction> {
    private final String identifier;
    private final String serverName;

    public ReliableAction(String serverName, Consumer<Acknowledgment> whenAcknowledged) {
        super(ActionsReceiver.PREFIX);

        this.identifier = RandomStringUtils.randomAlphanumeric(5);
        this.serverName = serverName;

        AckReceiver receiver = Core.getInstance().getAckReceiver();

        receiver.registerReliableAction(this, whenAcknowledged);
    }

    abstract Acknowledgment run();

    @Override
    protected void execute() {
        boolean desiredServer = Core.getInstance().getServerName().equalsIgnoreCase(serverName);

        if (desiredServer) {
            Acknowledgment acknowledgment = run();

            acknowledge(acknowledgment);
        }

    }

    private void acknowledge(Acknowledgment acknowledgment) {
        JedisPool pool = JedisManager.getInstance().getPool();
        String message = buildMessage(acknowledgment);

        try (Jedis jedis = pool.getResource()) {
            jedis.publish(AckReceiver.ACTION_ACKNOWLEDGE, message);
        }

    }

    public static String extractIdentifier(String message) {
        return message.split("/")[0];
    }

    public static Acknowledgment extractAcknowledgement(String message) {
        return Base64Serializer.deserialize(message.split("/")[1]);
    }

    public String buildMessage(Acknowledgment acknowledgment) {
        return identifier + "/" + Base64Serializer.serialize(acknowledgment);
    }

}
