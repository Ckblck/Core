package net.oldust.core.actions.reliable;

import net.oldust.core.actions.reliable.ack.Acknowledgment;

import java.util.function.Consumer;

/**
 * Actions used to be received in the proxy.
 * These actions SHOULD NOT BE USED in Spigot servers.
 */

public abstract class ProxyReliableAction extends ReliableAction {

    public ProxyReliableAction(String serverName, Consumer<Acknowledgment> whenAcknowledged) {
        super(serverName, whenAcknowledged);
    }

    @Override
    protected void execute() {
        run();
    }

}
