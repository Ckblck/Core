package net.oldust.core.actions.reliable;

import com.google.gson.Gson;
import lombok.Getter;
import net.oldust.core.Core;
import net.oldust.core.actions.Action;
import net.oldust.core.actions.ActionsReceiver;
import net.oldust.core.actions.reliable.ack.AckReceiver;
import net.oldust.core.actions.reliable.ack.Acknowledgment;
import net.oldust.sync.jedis.JedisManager;
import org.apache.commons.lang.RandomStringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * An action which acknowledges
 * the receive and proper execution
 * of a code block.
 * <p>
 * This class should be used in server environments,
 * for Bungee/Velocity, use: {@link ProxyReliableAction}.
 */

@Getter
public abstract class ReliableAction extends Action<ReliableAction> {
    private static final Gson GSON = new Gson();

    private final String identifier;
    private final String serverName;

    public ReliableAction(String serverName, Consumer<Acknowledgment> whenAcknowledged) {
        super(ActionsReceiver.PREFIX);

        this.identifier = RandomStringUtils.randomAlphanumeric(5);
        this.serverName = serverName;

        AckReceiver receiver = Core.getMultiplatformPlugin().getAckReceiver();

        receiver.registerReliableAction(this, whenAcknowledged);
    }

    public abstract CompletableFuture<Acknowledgment> run();

    @Override
    protected void execute() {
        boolean desiredServer = Core.getInstance().getServerName().equalsIgnoreCase(serverName);

        if (desiredServer) {
            CompletableFuture<Acknowledgment> acknowledgment = run();

            acknowledge(acknowledgment);
        }

    }

    private void acknowledge(CompletableFuture<Acknowledgment> acknowledgmentFut) {
        acknowledgmentFut.thenAccept(acknowledgment -> {
            JedisPool pool = JedisManager.getInstance().getPool();
            String message = buildMessage(acknowledgment);

            try (Jedis jedis = pool.getResource()) {
                jedis.publish(AckReceiver.ACTION_ACKNOWLEDGE, message);
            }
        });
    }

    public static String extractIdentifier(String message) {
        return message.split("/")[0];
    }

    public static Acknowledgment extractAcknowledgement(String message) {
        return GSON.fromJson(message.split("/")[1], Acknowledgment.class);
    }

    public String buildMessage(Acknowledgment acknowledgment) {
        return identifier + "/" + GSON.toJson(acknowledgment);
    }

}
