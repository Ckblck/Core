package net.oldust.core.actions.reliable.ack;

import java.io.Serializable;

@FunctionalInterface
public interface ReasonCreator extends Serializable {
    String getReason();
}
